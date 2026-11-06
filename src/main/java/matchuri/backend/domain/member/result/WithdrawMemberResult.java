package matchuri.backend.domain.member.result;

import matchuri.backend.domain.member.entity.Member;

public record WithdrawMemberResult(
        Long id,
        String status
) {

    public static WithdrawMemberResult from(Member member) {
        return new WithdrawMemberResult(
                member.getId(),
                member.getStatus().name()
        );
    }
}
