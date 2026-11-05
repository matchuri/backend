package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record GroupDetailV2Response(
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

        @Schema(description = "추천 기준 위치의 반경 거리(미터)입니다.", example = "1000")
        Integer radiusMeters,

        @Schema(description = "추천 기준 위치의 주소 문자열입니다.", example = "서울 강남구 테헤란로 123")
        String address,

        @Schema(description = "그룹 상태입니다.", example = "ACTIVE")
        GroupRoomStatus status,

        @Schema(description = "프로필 이미지 URL을 포함한 현재 그룹 멤버 목록입니다.")
        List<GroupMemberSummaryV2Response> members,

        @Schema(description = "가장 최근 그룹 추천입니다. 없으면 null입니다.")
        GroupRecommendationSessionResponse recentlyRecommendation
) {
}
