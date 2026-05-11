package matchuri.backend.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import matchuri.backend.domain.member.entity.SocialProviderType;

public record OAuth2ExchangeRequest(
        @Schema(
                description = "교환 코드가 속한 소셜 로그인 제공자입니다. 현재 지원 값은 GOOGLE, KAKAO입니다.",
                example = "KAKAO"
        )
        @NotNull(message = "provider는 비어 있을 수 없습니다.")
        SocialProviderType provider,

        @Schema(
                description = "OAuth2 로그인 성공 후 프론트가 전달받은 단기 교환 코드입니다. 한 번만 사용할 수 있습니다.",
                example = "temporary_exchange_code"
        )
        @NotBlank(message = "code는 비어 있을 수 없습니다.")
        String code
) {
}
