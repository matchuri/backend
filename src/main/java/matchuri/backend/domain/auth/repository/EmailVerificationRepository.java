package matchuri.backend.domain.auth.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@NullMarked
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByVerificationTokenHash(String verificationTokenHash);

    @Query("""
            select e
            from EmailVerification e
            where e.email = :email
              and e.purpose = :purpose
              and e.status = :status
              and ((:loginId is null and e.loginId is null) or e.loginId = :loginId)
            order by e.createdAt desc
            """)
    List<EmailVerification> findAllByTargetAndStatus(
            @Param("email") String email,
            @Param("purpose") EmailVerificationPurpose purpose,
            @Nullable @Param("loginId") String loginId,
            @Param("status") EmailVerificationStatus status
    );
}
