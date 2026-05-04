package matchuri.backend.domain.member.result;

import matchuri.backend.domain.member.entity.Member;

public record MemberProfileResult(
        Long id,
        String loginId,
        String nickname,
        boolean isSocial,
        String email
) {

    public static MemberProfileResult from(Member member) {
        return new MemberProfileResult(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.isSocial(),
                member.getEmail()
        );
    }
}
