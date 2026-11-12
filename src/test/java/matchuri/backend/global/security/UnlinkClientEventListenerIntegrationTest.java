package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import matchuri.backend.domain.auth.entity.OAuthUnlinkJob;
import matchuri.backend.domain.auth.entity.FailureCode;
import matchuri.backend.domain.auth.entity.Status;
import matchuri.backend.domain.auth.repository.OAuthUnlinkJobRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.service.MemberPurgeService;
import matchuri.backend.domain.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpServerErrorException;

/**
 * AFTER_COMMIT + 비동기 배선 검증. 클래스 레벨 @Transactional을 쓰면 커밋이 일어나지 않으므로 사용하지 않는다.
 */
@SpringBootTest(properties = {
        "matchuri.auth.oauth-unlink.poll-interval=24h",
        "matchuri.auth.oauth-unlink.retry-delays=5m,30m,2h"
})
@ActiveProfiles("test")
class UnlinkClientEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberPurgeService purgeService;

    @Autowired
    private OAuthUnlinkJobRepository jobRepository;

    @Autowired
    private OAuthUnlinkRetryScheduler retryScheduler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private OAuth2Unlinker unlinker;

    private Long memberId;

    @AfterEach
    void purgeTestMember() {
        if (memberId != null) {
            purgeService.purgeIfDue(memberId, now().plusDays(4));
        }
    }

    @Test
    @DisplayName("탈퇴 커밋 후 첫 외부 실패가 DB에 저장되고 재시도가 성공하면 작업이 삭제된다")
    void retriesAfterCommittedWithdrawalAndDeletesSuccessfulJob() {
        createSocialMember();
        doThrow(providerUnavailable()).doNothing().when(unlinker).unlink(memberId);

        LocalDateTime before = now();
        memberService.withdraw(memberId);

        OAuthUnlinkJob job = awaitFirstFailure();
        assertThat(memberRepository.findById(memberId).orElseThrow().getStatus()).isEqualTo(MemberStatus.DELETED);
        assertThat(job.getStatus()).isEqualTo(Status.PENDING);
        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getLastFailureCode()).isEqualTo(FailureCode.HTTP_5XX);
        assertThat(job.getLastHttpStatus()).isEqualTo(503);
        assertNextRetry(job, before, Duration.ofMinutes(5));

        retryScheduler.retryDueJobs();
        verify(unlinker, times(1)).unlink(memberId);

        makeJobDue();
        retryScheduler.retryDueJobs();

        verify(unlinker, times(2)).unlink(memberId);
        assertThat(jobRepository.findByMemberId(memberId)).isEmpty();
    }

    @Test
    @DisplayName("세 번 재시도한 최종 실패는 회원과 함께 물리 삭제될 때까지 보관된다")
    void exhaustsRetriesAndRetainsJobUntilPurge() {
        createSocialMember();
        doThrow(providerUnavailable()).when(unlinker).unlink(memberId);

        memberService.withdraw(memberId);
        awaitFirstFailure();

        makeJobDue();
        LocalDateTime beforeFirstRetry = now();
        retryScheduler.retryDueJobs();
        assertNextRetry(jobRepository.findByMemberId(memberId).orElseThrow(), beforeFirstRetry, Duration.ofMinutes(30));

        makeJobDue();
        LocalDateTime beforeSecondRetry = now();
        retryScheduler.retryDueJobs();
        assertNextRetry(jobRepository.findByMemberId(memberId).orElseThrow(), beforeSecondRetry, Duration.ofHours(2));

        makeJobDue();
        retryScheduler.retryDueJobs();
        retryScheduler.retryDueJobs();

        OAuthUnlinkJob job = jobRepository.findByMemberId(memberId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(Status.FAILED);
        assertThat(job.getAttemptCount()).isEqualTo(4);
        assertThat(job.getNextAttemptAt()).isNull();
        verify(unlinker, times(4)).unlink(memberId);

        assertThat(purgeService.purgeIfDue(memberId, now().plusDays(4))).isTrue();
        assertThat(jobRepository.findById(job.getId())).isEmpty();
    }

    @Test
    @DisplayName("물리 삭제 예정 시각이 지난 회원은 작업 행이 남아 있어도 재시도하지 않는다")
    void skipsDueJobAfterPurgeDeadline() {
        Member member = createSocialMember();
        LocalDateTime past = now().minusMinutes(1);
        member.withdraw(past.minusDays(3), past);
        member = memberRepository.saveAndFlush(member);
        jobRepository.saveAndFlush(OAuthUnlinkJob.firstFailure(member, FailureCode.TIMEOUT, null, past));

        retryScheduler.retryDueJobs();

        verify(unlinker, times(0)).unlink(memberId);
        assertThat(jobRepository.findByMemberId(memberId)).isPresent();
    }

    @Test
    @DisplayName("트랜잭션이 롤백되면 연결 해제를 실행하지 않는다")
    void doesNotUnlinkWhenTransactionRollsBack() {
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(new OAuthUnlinkEvent(20L));
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);

        verify(unlinker, after(500).never()).unlink(20L);
    }

    private Member createSocialMember() {
        Member member = memberRepository.saveAndFlush(
                Member.createSocialMember(SocialProviderType.KAKAO, UUID.randomUUID().toString(), null, null));
        memberId = member.getId();
        return member;
    }

    private OAuthUnlinkJob awaitFirstFailure() {
        return await().atMost(Duration.ofSeconds(5))
                .until(() -> jobRepository.findByMemberId(memberId), result -> result.isPresent())
                .orElseThrow();
    }

    private HttpServerErrorException providerUnavailable() {
        return HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "unavailable",
                new HttpHeaders(), "x".repeat(512).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private void makeJobDue() {
        jdbcTemplate.update("update oauth_unlink_job set next_attempt_at = ? where member_id = ?",
                now().minusSeconds(1), memberId);
    }

    private void assertNextRetry(OAuthUnlinkJob job, LocalDateTime before, Duration delay) {
        assertThat(job.getNextAttemptAt()).isBetween(before.plus(delay).minusNanos(1_000), now().plus(delay));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
