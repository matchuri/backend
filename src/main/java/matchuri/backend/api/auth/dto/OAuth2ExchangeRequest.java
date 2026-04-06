package matchuri.backend.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import matchuri.backend.domain.member.entity.SocialProviderType;

public record OAuth2ExchangeRequest(
        @NotNull(message = "provider는 비어 있을 수 없습니다.")
        SocialProviderType provider,

        @NotBlank(message = "code는 비어 있을 수 없습니다.")
        String code
) {
}
