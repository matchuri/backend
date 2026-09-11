package matchuri.backend.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import matchuri.backend.domain.auth.entity.AuthExchangeCode;
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.group.entity.GroupMemberRole;
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
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
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
    private EntityManager entityManager;

    @Test
    @DisplayName("탈퇴는 회원과 방장 그룹을 삭제 대기 상태로 전환하고 기존 세션을 폐기한다")
    void withdrawsMemberWithOwnedGroups() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 9, 11, 10, 0);
        Member member = saveMember("lifecycle-member", "생명주기회원");
        GroupRoom closedRoom = GroupRoom.createOwnedBy("닫힌 그룹", "LIFECYCLE-CODE", member);
        closedRoom.close();
        groupRoomRepository.saveAndFlush(closedRoom);
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
        assertThat(authRefreshTokenRepository.findByMemberId(member.getId())).isEmpty();
        assertThat(authExchangeCodeRepository.findByCode("lifecycle-exchange-code")).isEmpty();
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
