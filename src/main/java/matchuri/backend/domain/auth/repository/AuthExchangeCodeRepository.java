package matchuri.backend.domain.auth.repository;

import java.util.Optional;
import matchuri.backend.domain.auth.entity.AuthExchangeCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthExchangeCodeRepository extends JpaRepository<AuthExchangeCode, Long> {

    Optional<AuthExchangeCode> findByCode(String code);
}
