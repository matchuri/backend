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

    public void saveRefreshTokenLogic(Member member, String token) {
        boolean exists = repository.existsByMemberId(member.getId());
        if (!exists) {
            saveRefreshToken(member, token);
        }
    }

    public void saveRefreshToken(Member member, String token) {
        ProviderRefreshToken providerRefreshToken = ProviderRefreshToken.builder()
                .member(member)
                .token(token)
                .build();

        repository.save(providerRefreshToken);
    }

}
