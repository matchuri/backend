package matchuri.backend.global.security;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2UnlinkClientFactory {

    private final List<UnlinkClient> clients;

    public Optional<UnlinkClient> getClient(boolean isSocial, SocialProviderType socialProviderType) {
        for (UnlinkClient client : clients) {
            if (client.support(isSocial, socialProviderType)) {
                return Optional.of(client);
            }
        }
        return Optional.empty();
    }
}
