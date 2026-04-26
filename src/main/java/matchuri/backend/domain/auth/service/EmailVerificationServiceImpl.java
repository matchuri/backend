package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.EmailVerificationRequest;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender mailSender;

    @Override
    public void sendTxtEmail(EmailVerificationRequest request) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setTo(request.email());
        smm.setSubject(request.subject());
        smm.setText(request.content());
        try {
            mailSender.send(smm);
            System.out.println("이메일 전송 성공!");
        } catch (MailException e) {
            System.out.println("[-] 이메일 전송중에 오류가 발생하였습니다 " + e.getMessage());
            throw e;
        }
    }

}
