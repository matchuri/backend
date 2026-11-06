package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateGroupRequest(
        @Schema(description = "그룹 방 이름입니다.", example = "오늘 점심 메뉴 회의")
        @NotBlank(message = "name은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "name은 100자를 초과할 수 없습니다.")
        String name,

        @Schema(description = "그룹 추천 기준 위치의 위도입니다.", example = "37.498095")
        @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "latitude는 90 이하여야 합니다.")
        BigDecimal latitude,

        @Schema(description = "그룹 추천 기준 위치의 경도입니다.", example = "127.027610")
        @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "longitude는 180 이하여야 합니다.")
        BigDecimal longitude,

        @Schema(description = "그룹 추천 기준 위치의 반경 거리(미터)입니다.", example = "1000")
        @Min(value = 0, message = "radiusMeters는 0 이상이어야 합니다.")
        Integer radiusMeters,

        @Schema(description = "그룹 추천 기준 위치의 주소 문자열입니다.", example = "서울 강남구 테헤란로 123")
        @Size(max = 255, message = "address는 255자를 초과할 수 없습니다.")
        String address
) {
}
