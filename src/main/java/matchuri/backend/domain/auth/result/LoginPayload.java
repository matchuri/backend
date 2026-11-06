package matchuri.backend.domain.auth.result;

import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.result.OnboardingStatusResult;

public record LoginPayload(
        String accessToken,
        long expiresIn,
        Long memberId,
        String role,
        String nickname,
        OnboardingStatusResult onboarding
) {

    public static LoginPayload from(IssuedAccessToken issuedAccessToken, Member member,
                                    OnboardingStatusResult onboarding) {
        return new LoginPayload(
                issuedAccessToken.accessToken(),
                issuedAccessToken.expiresIn(),
                member.getId(),
                member.getMemberRole().name(),
                member.getNickname(),
                onboarding
        );
    }
}
