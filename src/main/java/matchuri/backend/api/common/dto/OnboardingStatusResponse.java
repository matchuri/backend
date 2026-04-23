package matchuri.backend.api.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.member.result.OnboardingNextStep;

public record OnboardingStatusResponse(
        @Schema(description = "최신 필수 약관 동의를 완료했는지 여부입니다.", example = "true")
        boolean requiredAgreementsCompleted,

        @Schema(description = "사용자가 닉네임 온보딩을 완료했는지 여부입니다.", example = "true")
        boolean nicknameCompleted,

        @Schema(description = "필수 온보딩 전체 완료 여부입니다.", example = "true")
        boolean completed,

        @Schema(description = "프론트가 이동해야 하는 다음 필수 단계입니다.", example = "READY")
        OnboardingNextStep nextStep
) {
}
