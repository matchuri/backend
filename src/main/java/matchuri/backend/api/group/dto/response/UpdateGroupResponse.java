package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record UpdateGroupResponse(
        @Schema(description = "수정된 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "수정된 그룹 이름입니다.", example = "점심 회의방")
        String name,

        @Schema(description = "수정된 그룹 추천 기준 위치의 위도입니다.", example = "37.498095")
        BigDecimal latitude,

        @Schema(description = "수정된 그룹 추천 기준 위치의 경도입니다.", example = "127.027610")
        BigDecimal longitude,

        @Schema(description = "수정된 그룹 추천 기준 위치의 반경 거리(미터)입니다.", example = "1000")
        Integer radiusMeters,

        @Schema(description = "수정된 그룹 추천 기준 위치의 주소 문자열입니다.", example = "서울 강남구 테헤란로 123")
        String address,

        @Schema(description = "그룹 상태입니다.", example = "ACTIVE")
        GroupRoomStatus status,

        @Schema(description = "수정 시각입니다.", example = "2026-05-18T12:30:00")
        LocalDateTime updatedAt
) {
}
