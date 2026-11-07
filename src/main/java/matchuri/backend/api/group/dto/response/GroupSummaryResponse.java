package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record GroupSummaryResponse(
        @Schema(description = "그룹 ID입니다.", example = "3001")
        Long id,

        @Schema(description = "그룹 이름입니다.", example = "오늘 점심 메뉴 회의")
        String name,

        @Schema(description = "그룹 상태입니다.", example = "ACTIVE")
        GroupRoomStatus status,

        @Schema(description = "활성 멤버 수입니다.", example = "4")
        Integer memberCount,

        @Schema(description = "최근 그룹 추천 상태입니다.", example = "OPEN")
        GroupRecommendationStatus latestRecommendationStatus,

        @Schema(description = "그룹 생성 시각입니다.", example = "2026-05-06T12:00:00")
        LocalDateTime createdAt
) {
    public static GroupSummaryResponse mockActive() {
        return new GroupSummaryResponse(
                3001L,
                "오늘 점심 메뉴 회의",
                GroupRoomStatus.ACTIVE,
                4,
                GroupRecommendationStatus.OPEN,
                LocalDateTime.of(2026, 5, 6, 12, 0)
        );
    }
}
