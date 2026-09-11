package matchuri.backend.domain.member.support.deletion;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberWithdrawalManager {

    public static final int DELETION_GRACE_PERIOD_DAYS = 3;

    private final MemberRepository memberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    private final AuthExchangeCodeRepository authExchangeCodeRepository;

    public Member withdraw(Long memberId, LocalDateTime deletedAt) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND, memberId));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.INACTIVE_MEMBER, member.getId());
        }

        member.withdraw(deletedAt, deletedAt.plusDays(DELETION_GRACE_PERIOD_DAYS));
        groupRoomRepository.findOwnedNotDeletedForUpdate(memberId).forEach(room -> room.delete(deletedAt));
        authRefreshTokenRepository.deleteByMemberId(memberId);
        authExchangeCodeRepository.deleteByMemberId(memberId);

        return member;
    }
}
