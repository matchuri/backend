package matchuri.backend.domain.member.result;

public record OnboardingStatusResult(
        boolean requiredAgreementsCompleted,
        boolean nicknameCompleted,
        boolean completed,
        OnboardingNextStep nextStep
) {

    public static OnboardingStatusResult of(boolean requiredAgreementsCompleted, boolean nicknameCompleted) {
        boolean completed = requiredAgreementsCompleted && nicknameCompleted;
        OnboardingNextStep nextStep;
        if (!requiredAgreementsCompleted) {
            nextStep = OnboardingNextStep.REQUIRED_AGREEMENTS;
        } else if (!nicknameCompleted) {
            nextStep = OnboardingNextStep.REQUIRED_NICKNAME;
        } else {
            nextStep = OnboardingNextStep.READY;
        }

        return new OnboardingStatusResult(requiredAgreementsCompleted, nicknameCompleted, completed, nextStep);
    }
}
