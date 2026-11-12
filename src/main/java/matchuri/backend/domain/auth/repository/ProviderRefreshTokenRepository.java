package matchuri.backend.domain.auth.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.auth.entity.ProviderRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRefreshTokenRepository extends JpaRepository<ProviderRefreshToken, Long> {
    List<ProviderRefreshToken> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    Optional<ProviderRefreshToken> findOneByMemberId(Long memberId);
}
