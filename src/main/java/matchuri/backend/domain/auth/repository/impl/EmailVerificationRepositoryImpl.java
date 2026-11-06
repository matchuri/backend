package matchuri.backend.domain.auth.repository.impl;

import static matchuri.backend.domain.auth.entity.QEmailVerification.emailVerification;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import matchuri.backend.domain.auth.repository.EmailVerificationRepositoryCustom;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<EmailVerification> findAllByTargetAndStatus(String email, EmailVerificationPurpose purpose, @Nullable String loginId, EmailVerificationStatus status) {
        return jpaQueryFactory
                .selectFrom(emailVerification)
                .where(
                        emailVerification.email.eq(email),
                        emailVerification.purpose.eq(purpose),
                        emailVerification.status.eq(status),
                        loginIdEquals(loginId)
                )
                .orderBy(emailVerification.createdAt.desc())
                .fetch();
    }

    private BooleanExpression loginIdEquals(@Nullable String loginId) {
        return loginId == null
                ? emailVerification.loginId.isNull()
                : emailVerification.loginId.eq(loginId);
    }
}
