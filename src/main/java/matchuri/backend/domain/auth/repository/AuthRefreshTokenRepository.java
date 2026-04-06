package matchuri.backend.domain.auth.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, Long> {

    Optional<AuthRefreshToken> findByToken(String token);

    List<AuthRefreshToken> findByMemberId(Long memberId);

    void deleteByToken(String token);
}
