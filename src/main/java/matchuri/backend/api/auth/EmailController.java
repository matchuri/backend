package matchuri.backend.api.auth;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.EmailSendRequest;
import matchuri.backend.api.auth.dto.response.EmailSendResponse;
import matchuri.backend.domain.auth.service.EmailVerificationService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class EmailController implements EmailApi {

    private final EmailVerificationService emailVerificationService;

    @Override
    @PostMapping("/email")
    public ApiResponse<EmailSendResponse> sendTxtEmail(EmailSendRequest request) {
        EmailSendResponse response = emailVerificationService.sendTxtEmail(request);
        return ApiResponse.success(response);
    }
}
