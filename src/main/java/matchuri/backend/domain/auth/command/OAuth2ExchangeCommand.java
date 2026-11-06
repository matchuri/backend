package matchuri.backend.domain.auth.command;

import matchuri.backend.domain.member.entity.SocialProviderType;

public record OAuth2ExchangeCommand(
        SocialProviderType provider,
        String code
) {
}
