package matchuri.backend.domain.auth.result;

import matchuri.backend.domain.member.entity.Member;

public record LoginPayload(
        String accessToken,
        long expiresIn,
        Long memberId,
        String role
) {

    public static LoginPayload from(IssuedAccessToken issuedAccessToken, Member member) {
        return new LoginPayload(
                issuedAccessToken.accessToken(),
                issuedAccessToken.expiresIn(),
                member.getId(),
                member.getMemberRole().name()
        );
    }
}
