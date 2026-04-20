package matchuri.backend.domain.auth.result;

import matchuri.backend.domain.member.entity.Member;

public record LoginResult(
        LoginPayload payload,
        String refreshToken
) {

    public static LoginResult from(TokenPair tokenPair, Member member) {
        return new LoginResult(
                new LoginPayload(
                        tokenPair.accessToken(),
                        tokenPair.accessTokenExpiresIn(),
                        member.getId(),
                        member.getMemberRole().name()
                ),
                tokenPair.refreshToken()
        );
    }
}
