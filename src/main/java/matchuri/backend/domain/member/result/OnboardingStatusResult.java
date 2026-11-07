package matchuri.backend.domain.member.result;

public record OnboardingStatusResult(
        boolean requiredAgreementsCompleted,
        boolean nicknameCompleted,
        boolean tasteProfileCompleted,
        boolean completed,
        OnboardingNextStep nextStep
) {

    public static OnboardingStatusResult of(boolean requiredAgreementsCompleted, boolean nicknameCompleted, boolean tasteProfileCompleted) {
        boolean completed = requiredAgreementsCompleted && nicknameCompleted && tasteProfileCompleted;
        OnboardingNextStep nextStep;

        if (!requiredAgreementsCompleted) {
            nextStep = OnboardingNextStep.REQUIRED_AGREEMENTS;
        } else if (!nicknameCompleted) {
            nextStep = OnboardingNextStep.REQUIRED_NICKNAME;
        } else if (!tasteProfileCompleted) {
            nextStep = OnboardingNextStep.REQUIRED_TASTE_PROFILE;
        } else {
            nextStep = OnboardingNextStep.READY;
        }

        return new OnboardingStatusResult(requiredAgreementsCompleted, nicknameCompleted, tasteProfileCompleted, completed, nextStep);
    }
}
