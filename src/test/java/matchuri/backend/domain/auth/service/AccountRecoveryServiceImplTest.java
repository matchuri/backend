package matchuri.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import matchuri.backend.domain.auth.command.FindLoginIdCommand;
import matchuri.backend.domain.auth.command.ResetPasswordCommand;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.auth.support.verification.EmailVerificationTokenVerifier;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryServiceImplTest {

    @Mock
    private EmailVerificationTokenVerifier emailVerificationTokenVerifier;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountRecoveryServiceImpl accountRecoveryService;

    @Test
    @DisplayName("FIND_LOGIN_ID token의 이메일에 연결된 활성 자체 로그인 ID를 반환한다")
    void findLoginIdReturnsLocalLoginId() {
        EmailVerification verification = verifiedFindLoginIdVerification("tester@example.com");
        Member member = new Member(
                "tester01",
                "hashed-password",
                "tester@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        );

        when(emailVerificationTokenVerifier.verifyFindLoginIdToken("ev_find-token")).thenReturn(verification);
        when(memberRepository.findByEmailAndSocialFalseAndStatus("tester@example.com", MemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));

        var result = accountRecoveryService.findLoginId(new FindLoginIdCommand("ev_find-token"));

        assertThat(result.loginId()).isEqualTo("tester01");
    }

    @Test
    @DisplayName("token은 유효하지만 활성 자체 로그인 계정이 없으면 인증 실패로 처리한다")
    void findLoginIdFailsWhenLocalMemberDoesNotExist() {
        EmailVerification verification = verifiedFindLoginIdVerification("missing@example.com");

        when(emailVerificationTokenVerifier.verifyFindLoginIdToken("ev_find-token")).thenReturn(verification);
        when(memberRepository.findByEmailAndSocialFalseAndStatus("missing@example.com", MemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountRecoveryService.findLoginId(new FindLoginIdCommand("ev_find-token")))
                .isInstanceOf(AuthenticationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EMAIL_VERIFICATION_FAILED);
    }

    @Test
    @DisplayName("RESET_PASSWORD token의 계정 비밀번호를 교체하고 기존 refresh token을 모두 폐기한다")
    void resetPasswordUpdatesPasswordAndRevokesRefreshTokens() {
        EmailVerification verification = verifiedResetPasswordVerification("tester@example.com", "tester01");
        Member member = new Member(
                "tester01",
                "old-hash",
                "tester@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        );
        AuthRefreshToken refreshToken = AuthRefreshToken.issue(member, "old-refresh-token", LocalDateTime.now().plusDays(7));

        when(emailVerificationTokenVerifier.verifyResetPasswordToken("tester01", "ev_reset-token"))
                .thenReturn(verification);
        when(memberRepository.findByLoginIdAndEmailAndSocialFalseAndStatus(
                "tester01",
                "tester@example.com",
                MemberStatus.ACTIVE
        )).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("N3wP@ssw0rd!")).thenReturn("new-hash");
        when(authRefreshTokenRepository.findByMemberId(member.getId())).thenReturn(java.util.List.of(refreshToken));

        var result = accountRecoveryService.resetPassword(new ResetPasswordCommand(
                "tester01",
                "ev_reset-token",
                "N3wP@ssw0rd!"
        ));

        assertThat(result.reset()).isTrue();
        assertThat(member.getPasswordHash()).isEqualTo("new-hash");
        org.mockito.Mockito.verify(authRefreshTokenRepository).deleteAll(java.util.List.of(refreshToken));
    }

    private EmailVerification verifiedFindLoginIdVerification(String email) {
        EmailVerification verification = EmailVerification.issue(
                email,
                null,
                EmailVerificationPurpose.FIND_LOGIN_ID,
                "hashed-code",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now()
        );
        verification.verify("hashed-token", LocalDateTime.now().plusMinutes(10), LocalDateTime.now());
        return verification;
    }

    private EmailVerification verifiedResetPasswordVerification(String email, String loginId) {
        EmailVerification verification = EmailVerification.issue(
                email,
                loginId,
                EmailVerificationPurpose.RESET_PASSWORD,
                "hashed-code",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now()
        );
        verification.verify("hashed-token", LocalDateTime.now().plusMinutes(10), LocalDateTime.now());
        return verification;
    }
}
