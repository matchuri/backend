package matchuri.backend.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import matchuri.backend.domain.auth.entity.AuthExchangeCode;
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.ProviderRefreshToken;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.auth.repository.ProviderRefreshTokenRepository;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.service.MemberPurgeService;
import matchuri.backend.domain.member.support.deletion.MemberWithdrawalManager;
import matchuri.backend.global.security.KakaoUnlinkClient;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(properties = "kakao.admin-key=test-kakao-admin-key")
@ActiveProfiles("test")
@Transactional
class MemberDeletionIntegrationTest {

    @Autowired
    private MemberWithdrawalManager withdrawalManager;

    @Autowired
    private MemberPurgeService memberPurgeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRoomRepository groupRoomRepository;

    @Autowired
    private GroupRoomMemberRepository groupRoomMemberRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private AuthExchangeCodeRepository authExchangeCodeRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private ProviderRefreshTokenRepository providerRefreshTokenRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("탈퇴는 회원과 방장 그룹을 삭제 대기 상태로 전환하고 기존 세션을 폐기한다")
    void withdrawsMemberWithOwnedGroups() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 9, 11, 10, 0);
        Member member = saveMember("lifecycle-member", "생명주기회원");
        GroupRoom closedRoom = GroupRoom.createOwnedBy("닫힌 그룹", "LIFECYCLE-CODE", member);
        closedRoom.close();
        groupRoomRepository.saveAndFlush(closedRoom);
        Member otherOwner = saveMember("lifecycle-other-owner", "다른방장");
        GroupRoom activeOtherRoom = GroupRoom.createOwnedBy("참여 그룹", "LIFECYCLE-OTHER-CODE", otherOwner);
        activeOtherRoom.addGroupMember(member, GroupMemberRole.MEMBER);
        groupRoomRepository.saveAndFlush(activeOtherRoom);
        GroupRoom closedOtherRoom = GroupRoom.createOwnedBy("닫힌 참여 그룹", "LIFECYCLE-CLOSED-CODE", otherOwner);
        closedOtherRoom.addGroupMember(member, GroupMemberRole.MEMBER);
        closedOtherRoom.close();
        groupRoomRepository.saveAndFlush(closedOtherRoom);
        authRefreshTokenRepository.save(AuthRefreshToken.issue(
                member,
                "lifecycle-refresh-token",
                requestedAt.plusDays(14)
        ));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "lifecycle-exchange-code",
                requestedAt.plusMinutes(5)
        ));

        Member deletedMember = withdrawalManager.withdraw(member.getId(), requestedAt);

        assertThat(deletedMember.getStatus()).isEqualTo(MemberStatus.DELETED);
        assertThat(deletedMember.getDeletedAt()).isEqualTo(requestedAt);
        assertThat(deletedMember.getPurgeAt()).isEqualTo(requestedAt.plusDays(3));
        assertThat(closedRoom.getStatus()).isEqualTo(GroupRoomStatus.DELETED);
        assertThat(closedRoom.getDeletedAt()).isEqualTo(requestedAt);
        assertThat(groupRoomMemberRepository.findActiveMembersByRoomId(closedRoom.getId())).isEmpty();
        assertThat(groupRoomMemberRepository.findByRoomIdAndMemberId(activeOtherRoom.getId(), member.getId())
                .orElseThrow().getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        assertThat(groupRoomMemberRepository.findByRoomIdAndMemberId(closedOtherRoom.getId(), member.getId())
                .orElseThrow().getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        assertThat(authRefreshTokenRepository.findByMemberId(member.getId())).isEmpty();
        assertThat(authExchangeCodeRepository.findByCode("lifecycle-exchange-code")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(SocialProviderType.class)
    @DisplayName("소셜 회원 탈퇴는 내부 회원 ID로 제공자 정보를 조회하고 연결 해제 요청을 전송한다")
    void withdrawsSocialMemberWithProviderUnlink(SocialProviderType provider) {
        Member member = memberRepository.saveAndFlush(Member.createSocialMember(
                provider, "987654321", "social@example.com", "소셜탈퇴회원"));
        if (provider != SocialProviderType.KAKAO) {
            providerRefreshTokenRepository.saveAndFlush(ProviderRefreshToken.builder()
                    .member(member).token("provider-refresh-token").build());
        }

        var expectedBody = new LinkedMultiValueMap<String, String>();
        String expectedUrl;
        Class<?> responseType;
        switch (provider) {
            case GOOGLE -> {
                expectedUrl = "https://oauth2.googleapis.com/revoke";
                expectedBody.add("token", "provider-refresh-token");
                responseType = Void.class;
            }
            case NAVER -> {
                expectedUrl = "https://nid.naver.com/oauth2.0/revoke";
                expectedBody.add("client_id", "test-naver-client-id");
                expectedBody.add("client_secret", "test-naver-client-secret");
                expectedBody.add("token", "provider-refresh-token");
                expectedBody.add("token_type_hint", "refresh_token");
                responseType = Void.class;
            }
            case KAKAO -> {
                expectedUrl = "https://kapi.kakao.com/v1/user/unlink";
                expectedBody.add("target_id_type", "user_id");
                expectedBody.add("target_id", "987654321");
                responseType = KakaoUnlinkClient.UnlinkResult.class;
            }
            default -> throw new IllegalArgumentException("Unsupported test provider: " + provider);
        }

        try (var http = mockConstruction(RestTemplate.class, (mock, context) -> {
            ResponseEntity<?> response = provider == SocialProviderType.KAKAO
                    ? ResponseEntity.ok(new KakaoUnlinkClient.UnlinkResult(987654321L))
                    : ResponseEntity.ok().build();
            doReturn(response).when(mock).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(responseType));
        })) {
            Member deleted = withdrawalManager.withdraw(member.getId(), LocalDateTime.of(2026, 11, 9, 12, 0));

            assertThat(deleted.getStatus()).isEqualTo(MemberStatus.DELETED);
            assertThat(http.constructed()).hasSize(1);
            ArgumentCaptor<HttpEntity<?>> request = ArgumentCaptor.captor();
            verify(http.constructed().getFirst()).exchange(eq(expectedUrl), eq(HttpMethod.POST), request.capture(), eq(responseType));
            assertThat(request.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);
            assertThat(request.getValue().getBody()).isEqualTo(expectedBody);
            if (provider == SocialProviderType.KAKAO) {
                assertThat(request.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                        .isEqualTo("KakaoAK test-kakao-admin-key");
            }
        }
    }

    @Test
    @DisplayName("삭제 예정 시각이 지난 회원은 소유 그룹과 다른 그룹의 회원 참조까지 물리 삭제한다")
    void purgesDueMemberGraph() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 15, 10, 0);
        Member deletedMember = saveMember("purge-member", "삭제대상회원");
        Member remainingOwner = saveMember("remaining-owner", "잔존방장");

        GroupRoom ownedRoom = GroupRoom.createOwnedBy("삭제 그룹", "PURGE-OWNED-CODE", deletedMember);
        groupRoomRepository.saveAndFlush(ownedRoom);
        GroupRoom remainingRoom = GroupRoom.createOwnedBy("잔존 그룹", "PURGE-REMAIN-CODE", remainingOwner);
        remainingRoom.addGroupMember(deletedMember, GroupMemberRole.MEMBER);
        groupRoomRepository.saveAndFlush(remainingRoom);

        Long deletedMemberId = deletedMember.getId();
        Long ownedRoomId = ownedRoom.getId();
        Long remainingRoomId = remainingRoom.getId();
        AuthRefreshToken refreshToken = authRefreshTokenRepository.saveAndFlush(AuthRefreshToken.issue(
                deletedMember,
                "purge-refresh-token",
                now.plusDays(7)
        ));
        AuthExchangeCode exchangeCode = authExchangeCodeRepository.saveAndFlush(AuthExchangeCode.issue(
                deletedMember,
                SocialProviderType.GOOGLE,
                "purge-exchange-code",
                now.plusMinutes(5)
        ));
        EmailVerification emailVerification = EmailVerification.issue(
                deletedMember.getEmail(),
                deletedMember.getLoginId(),
                EmailVerificationPurpose.RESET_PASSWORD,
                "purge-code-hash",
                now.plusMinutes(5),
                now
        );
        emailVerification.assignMember(deletedMember);
        emailVerificationRepository.saveAndFlush(emailVerification);
        Long refreshTokenId = refreshToken.getId();
        Long exchangeCodeId = exchangeCode.getId();
        Long emailVerificationId = emailVerification.getId();
        deletedMember.withdraw(now.minusDays(4), now.minusHours(1));
        ownedRoom.delete(now.minusDays(4));
        memberRepository.flush();
        groupRoomRepository.flush();

        QueryCountHolder.clear();
        assertThat(memberPurgeService.purgeIfDue(deletedMemberId, now)).isTrue();
        QueryCount purgeQueryCount = QueryCountHolder.getGrandTotal();
        assertThat(purgeQueryCount.getDelete()).isEqualTo(1);
        entityManager.clear();

        assertThat(memberRepository.findById(deletedMemberId)).isEmpty();
        assertThat(groupRoomRepository.findById(ownedRoomId)).isEmpty();
        assertThat(groupRoomRepository.findById(remainingRoomId)).isPresent();
        assertThat(groupRoomMemberRepository.findByRoomIdAndMemberId(remainingRoomId, deletedMemberId)).isEmpty();
        assertThat(authRefreshTokenRepository.findById(refreshTokenId)).isEmpty();
        assertThat(authExchangeCodeRepository.findById(exchangeCodeId)).isEmpty();
        assertThat(emailVerificationRepository.findById(emailVerificationId)).isEmpty();
        assertThat(memberRepository.findById(remainingOwner.getId())).isPresent();
    }

    private Member saveMember(String loginId, String nickname) {
        return memberRepository.saveAndFlush(
                Member.createWithEncodedPassword(loginId, "encoded-password", nickname, loginId + "@example.com")
        );
    }
}
