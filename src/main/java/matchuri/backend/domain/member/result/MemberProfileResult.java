package matchuri.backend.domain.member.result;

import matchuri.backend.domain.member.entity.Member;

public record MemberProfileResult(
        Long id,
        String loginId,
        String nickname,
        boolean isSocial,
        String email,
        String profileImageUrl
) {

    public static MemberProfileResult from(Member member, String profileImageUrl) {
        return new MemberProfileResult(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.isSocial(),
                member.getEmail(),
                profileImageUrl
        );
    }
}
