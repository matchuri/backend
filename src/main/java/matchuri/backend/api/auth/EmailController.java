package matchuri.backend.api.auth;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.EmailVerificationRequest;
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
    public ApiResponse<Void> sendTxtEmail(EmailVerificationRequest request) {
        emailVerificationService.sendTxtEmail(request);
        return ApiResponse.success(null);
    }
}
