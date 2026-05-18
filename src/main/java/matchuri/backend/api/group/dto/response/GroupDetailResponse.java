package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record GroupDetailResponse(
        @Schema(description = "그룹 ID입니다.", example = "3001")
        Long id,

        @Schema(description = "그룹 이름입니다.", example = "오늘 점심 메뉴 회의")
        String name,

        @Schema(description = "그룹 고정 초대 코드입니다.", example = "LUNCH42")
        String inviteCode,

        @Schema(description = "추천 기준 위치의 위도입니다.", example = "37.498095")
        BigDecimal latitude,

        @Schema(description = "추천 기준 위치의 경도입니다.", example = "127.027610")
        BigDecimal longitude,

        @Schema(description = "그룹 상태입니다.", example = "ACTIVE")
        GroupRoomStatus status,

        @Schema(description = "현재 그룹 멤버 목록입니다.")
        List<GroupMemberSummaryResponse> members,

        @Schema(description = "진행 중인 그룹 추천입니다. 없으면 null입니다.")
        GroupRecommendationSessionResponse activeRecommendation
) {
    public static GroupDetailResponse mockActive() {
        return new GroupDetailResponse(
                3001L,
                "오늘 점심 메뉴 회의",
                "LUNCH42",
                new BigDecimal("37.498095"),
                new BigDecimal("127.027610"),
                GroupRoomStatus.ACTIVE,
                GroupMocks.members(),
                GroupRecommendationSessionResponse.mockOpen()
        );
    }
}
