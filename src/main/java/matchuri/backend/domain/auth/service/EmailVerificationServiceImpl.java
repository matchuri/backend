package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.EmailVerificationRequest;
import matchuri.backend.domain.auth.support.vertification.VerificationCodeGenerator;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender mailSender;

    public static String EMAIL_SUBJECT = "맛추리 회원가입 인증 요청";

    @Override
    public void sendTxtEmail(EmailVerificationRequest request) {

        SimpleMailMessage smm = new SimpleMailMessage();
        String code = VerificationCodeGenerator.generateCode();

        smm.setTo(request.email());
        smm.setSubject(EMAIL_SUBJECT);
        smm.setText(code);

        try {
            mailSender.send(smm);
            System.out.println("이메일 전송 성공!");
        } catch (MailException e) {
            System.out.println("[-] 이메일 전송중에 오류가 발생하였습니다 " + e.getMessage());
            throw e;
        }
    }

}
