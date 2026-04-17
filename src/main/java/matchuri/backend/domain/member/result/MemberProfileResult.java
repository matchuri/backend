package matchuri.backend.domain.member.result;

import matchuri.backend.domain.member.entity.Member;

public record MemberProfileResult(
        Long id,
        String nickname
) {

    public static MemberProfileResult from(Member member) {
        return new MemberProfileResult(
                member.getId(),
                member.getNickname()
        );
    }
}
