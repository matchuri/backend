package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.api.auth.dto.request.EmailSendRequest;
import matchuri.backend.api.auth.dto.response.EmailSendResponse;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationType;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.auth.support.vertification.VerificationCodeGenerator;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository repository;

    public static String EMAIL_SUBJECT = "맛추리 인증 요청";

    @Override
    public EmailSendResponse sendTxtEmail(EmailSendRequest request) {

        SimpleMailMessage smm = new SimpleMailMessage();
        String email = request.email();
        EmailVerificationType type = EmailVerificationType.valueOf(request.type());
        String code = VerificationCodeGenerator.generateCode();

        smm.setTo(email);
        smm.setSubject(EMAIL_SUBJECT);
        smm.setText(code);

        EmailVerification emailVerification = EmailVerification.from(email, code, type);
        EmailVerification saved = repository.save(emailVerification);

        EmailSendResponse response = new EmailSendResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getType().name()
        );

        try {
            mailSender.send(smm);
            log.info("이메일 전송 성공!");
            return response;
        } catch (MailException e) {
            log.info("[-] 이메일 전송중에 오류가 발생하였습니다 {}", e.getMessage());
            throw e;
        }
    }

}
