package matchuri.backend.domain.auth.result;

import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.result.OnboardingStatusResult;

public record LoginResult(
        LoginPayload payload,
        String refreshToken
) {

    public static LoginResult from(TokenPair tokenPair, Member member, OnboardingStatusResult onboarding) {
        return new LoginResult(
                new LoginPayload(
                        tokenPair.accessToken(),
                        tokenPair.accessTokenExpiresIn(),
                        member.getId(),
                        member.getMemberRole().name(),
                        member.getNickname(),
                        onboarding
                ),
                tokenPair.refreshToken()
        );
    }
}
