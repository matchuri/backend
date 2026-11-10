package matchuri.backend.domain.auth.support.token;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.ProviderRefreshToken;
import matchuri.backend.domain.auth.repository.ProviderRefreshTokenRepository;
import matchuri.backend.domain.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderTokenService {

    private final ProviderRefreshTokenRepository repository;
    private final ProviderTokenCipher tokenCipher;

    public void upsert(Member member, String token) {
        if (token == null || token.isEmpty()) return;

        ProviderRefreshToken existing = repository.findOneByMemberId(member.getId());
        String encryptedToken = tokenCipher.encrypt(token);

        if (existing != null) {
            existing.updateToken(token);
            return;
        }

        save(member, encryptedToken);
    }

    public void save(Member member, String token) {
        ProviderRefreshToken providerRefreshToken = ProviderRefreshToken.builder()
                .member(member)
                .token(token)
                .build();

        repository.save(providerRefreshToken);
    }

    public String findByMemberId(Long memberId) {
        ProviderRefreshToken token = repository.findOneByMemberId(memberId);
        return tokenCipher.decrypt(token.getToken());
    }
}
