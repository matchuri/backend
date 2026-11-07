package matchuri.backend.api.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.member.result.OnboardingNextStep;

public record OnboardingStatusResponse(
        @Schema(description = "최신 필수 약관 동의를 완료했는지 여부입니다.", example = "true")
        boolean requiredAgreementsCompleted,

        @Schema(description = "사용자가 닉네임 온보딩을 완료했는지 여부입니다.", example = "true")
        boolean nicknameCompleted,

        @Schema(description = "취향 프로필을 저장했는지 여부입니다. 선택 목록이 비어 있어도 저장했다면 완료입니다.", example = "true")
        boolean tasteProfileCompleted,

        @Schema(description = "필수 약관, 닉네임, 취향 프로필 온보딩 전체 완료 여부입니다.", example = "true")
        boolean completed,

        @Schema(description = "필수 약관, 닉네임, 취향 프로필 순으로 판단하며, 모두 완료하면 READY입니다.", example = "READY")
        OnboardingNextStep nextStep
) {
}
