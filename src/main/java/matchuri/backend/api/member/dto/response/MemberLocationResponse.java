package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record MemberLocationResponse(
        @Schema(description = "개인 추천 기준 위치의 위도입니다.", example = "37.498095")
        BigDecimal latitude,
        @Schema(description = "개인 추천 기준 위치의 경도입니다.", example = "127.027610")
        BigDecimal longitude,
        @Schema(description = "개인 추천 기준 위치의 반경 거리(미터)입니다.", example = "1000")
        Integer radiusMeters,
        @Schema(description = "개인 추천 기준 위치의 주소 문자열입니다.", example = "서울 강남구 테헤란로 123")
        String address
) {
}
