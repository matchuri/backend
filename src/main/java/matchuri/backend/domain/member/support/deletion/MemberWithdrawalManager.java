package matchuri.backend.domain.member.support.deletion;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.support.room.GroupMemberDepartureManager;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.AuthenticationException;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.OAuth2UnlinkClientFactory;
import matchuri.backend.global.security.UnlinkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberWithdrawalManager {

    @Value("${matchuri.member-deletion.grace-period-day}")
    public int DELETION_GRACE_PERIOD_DAYS;

    private final MemberRepository memberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupMemberDepartureManager groupMemberDepartureManager;
    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    private final AuthExchangeCodeRepository authExchangeCodeRepository;
    private final OAuth2UnlinkClientFactory oAuth2UnlinkClientFactory;

    public Member withdraw(Long memberId, LocalDateTime deletedAt) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND, memberId));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.INACTIVE_MEMBER, member.getId());
        }

        member.withdraw(deletedAt, deletedAt.plusDays(DELETION_GRACE_PERIOD_DAYS));
        groupRoomRepository.findOwnedNotDeletedForUpdate(memberId).forEach(room -> room.delete(deletedAt));
        groupRoomMemberRepository.findActiveMembershipsInOtherRooms(memberId)
                .forEach(membership -> groupMemberDepartureManager.leave(membership, deletedAt));

        deleteTokens(memberId);
        oauth2Unlink(member);

        return member;
    }

    public void deleteTokens(Long memberId) {
        authRefreshTokenRepository.deleteByMemberId(memberId);
        authExchangeCodeRepository.deleteByMemberId(memberId);
    }

    public void oauth2Unlink(Member member) {
        if (!member.isSocial()) return;
        Optional<UnlinkClient> client = oAuth2UnlinkClientFactory.getClient(member.isSocial(), member.getSocialProviderType());
        client.ifPresentOrElse(
                unlinkClient -> unlinkClient.unlink(member.getSocialProviderUserId()),
                () -> { throw new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED); }
        );
    }
}
