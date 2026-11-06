package matchuri.backend.domain.auth.repository;

import java.util.List;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import org.jspecify.annotations.Nullable;

public interface EmailVerificationRepositoryCustom {

    List<EmailVerification> findAllByTargetAndStatus(String email, EmailVerificationPurpose purpose, @Nullable String loginId, EmailVerificationStatus status);
}
