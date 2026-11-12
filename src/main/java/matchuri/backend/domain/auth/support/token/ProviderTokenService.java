package matchuri.backend.domain.auth.support.token;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.ProviderRefreshToken;
import matchuri.backend.domain.auth.repository.ProviderRefreshTokenRepository;
import matchuri.backend.domain.member.entity.Member;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderTokenService {

    private final ProviderRefreshTokenRepository repository;
    private final ProviderTokenCipher tokenCipher;

    public void upsert(Member member, String token) {
        if (token == null || token.isEmpty()) return;
        String encryptedToken = tokenCipher.encrypt(token);

        repository.findOneByMemberId(member.getId()).ifPresentOrElse(
                existing -> existing.updateToken(encryptedToken),
                () -> save(member, encryptedToken)
        );
    }

    public void save(Member member, String token) {
        ProviderRefreshToken providerRefreshToken = ProviderRefreshToken.builder()
                .member(member)
                .token(token)
                .build();

        repository.save(providerRefreshToken);
    }

    public @Nullable String findByMemberId(Long memberId) {
        return repository.findOneByMemberId(memberId)
                .map(ProviderRefreshToken::getToken)
                .filter(token -> !token.isBlank())
                .map(tokenCipher::decrypt)
                .orElse(null);
    }
}
