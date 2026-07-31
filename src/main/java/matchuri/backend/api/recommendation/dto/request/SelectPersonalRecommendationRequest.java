package matchuri.backend.api.recommendation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import matchuri.backend.domain.member.entity.MemberLocation;

public record SelectPersonalRecommendationRequest(
        @Schema(description = "최종 선택할 개인 추천 후보 ID입니다.", example = "10001")
        @NotNull(message = "selectedCandidateId는 null일 수 없습니다.")
        @Positive(message = "selectedCandidateId는 양수여야 합니다.")
        Long selectedCandidateId,

        @Schema(description = "후보 확정 시점 위치의 위도입니다. 위치 정보가 모두 전달된 경우에만 저장합니다.", example = "37.498095", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "latitude는 90 이하여야 합니다.")
        BigDecimal latitude,

        @Schema(description = "후보 확정 시점 위치의 경도입니다. 위치 정보가 모두 전달된 경우에만 저장합니다.", example = "127.027610", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "longitude는 180 이하여야 합니다.")
        BigDecimal longitude,

        @Schema(description = "후보 확정 시점의 검색 반경(미터)입니다. 위치 정보가 모두 전달된 경우에만 저장합니다.", example = "1000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Min(value = 0, message = "radiusMeters는 0 이상이어야 합니다.")
        Integer radiusMeters,

        @Schema(description = "후보 확정 시점 위치의 주소입니다. 위치 정보가 모두 전달된 경우에만 저장합니다.", example = "서울 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(regexp = "(?s).*\\S.*", message = "address는 비어 있을 수 없습니다.")
        @Size(max = MemberLocation.ADDRESS_MAX_LENGTH, message = "address는 255자를 초과할 수 없습니다.")
        String address
) {
}
