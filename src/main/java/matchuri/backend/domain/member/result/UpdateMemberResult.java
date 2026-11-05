package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.member.entity.Member;

public record UpdateMemberResult(
        Long id,
        LocalDateTime updatedAt,
        OnboardingStatusResult onboarding
) {

    public static UpdateMemberResult from(Member member, OnboardingStatusResult onboarding) {
        return new UpdateMemberResult(
                member.getId(),
                member.getUpdatedAt(),
                onboarding
        );
    }
}
