package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupRecommendationReadinessProgressResponse(
        @Schema(description = "현재 준비 대상인 활성 그룹 멤버 수입니다.", example = "4")
        int totalMemberCount,

        @Schema(description = "준비 완료 상태인 활성 그룹 멤버 수입니다.", example = "3")
        int readyMemberCount,

        @Schema(description = "모든 현재 활성 그룹 멤버가 준비 완료했는지 여부입니다.", example = "false")
        boolean allReady
) {
}
