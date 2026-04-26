package matchuri.backend.domain.auth.support.mail;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuthMailSender {

    private static final String EMAIL_SUBJECT = "맛추리 이메일 인증 코드";

    private final JavaMailSender mailSender;
    private final MatchuriProperties matchuriProperties;

    public void sendVerificationEmail(String email, EmailVerificationPurpose purpose, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        String from = matchuriProperties.getAuth().getEmailVerification().getFrom();

        if (StringUtils.hasText(from)) {
            message.setFrom(from);
        }
        message.setTo(email);
        message.setSubject(EMAIL_SUBJECT);
        message.setText("""
                맛추리 이메일 인증 코드입니다.

                인증 목적: %s
                인증 코드: %s

                인증 코드는 5분 동안만 사용할 수 있습니다.
                """.formatted(purpose.name(), code));

        mailSender.send(message);
    }
}
