package matchuri.backend.domain.auth.repository;

import matchuri.backend.domain.auth.entity.EmailVerification;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
}
