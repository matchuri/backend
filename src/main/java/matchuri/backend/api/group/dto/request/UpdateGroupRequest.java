package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateGroupRequest(
        @Schema(description = "변경할 그룹 방 이름입니다. 생략하면 변경하지 않습니다.", example = "점심 회의방")
        @Size(max = 100, message = "name은 100자를 초과할 수 없습니다.")
        String name,

        @Schema(description = "변경할 그룹 추천 기준 위치의 위도입니다. 생략하면 변경하지 않습니다.", example = "37.498095")
        @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "latitude는 90 이하여야 합니다.")
        BigDecimal latitude,

        @Schema(description = "변경할 그룹 추천 기준 위치의 경도입니다. 생략하면 변경하지 않습니다.", example = "127.027610")
        @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "longitude는 180 이하여야 합니다.")
        BigDecimal longitude
) {

    @Schema(hidden = true)
    @AssertTrue(message = "name은 비어 있을 수 없습니다.")
    public boolean isNameNullOrNotBlank() {
        return name == null || !name.isBlank();
    }
}
