package matchuri.backend.api.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.SendEmailRequest;
import matchuri.backend.api.auth.dto.response.SendEmailResponse;
import matchuri.backend.domain.auth.command.SendEmailVerificationCommand;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.service.EmailVerificationService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.exception.RequestValidationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class EmailController implements EmailApi {

    private final EmailVerificationService emailVerificationService;

    @Override
    @PostMapping("/email")
    public ApiResponse<SendEmailResponse> sendVerificationEmail(@Valid @RequestBody SendEmailRequest request) {
        validateConditionalFields(request);

        var command = new SendEmailVerificationCommand(
                request.email(),
                request.purpose(),
                request.loginId()
        );
        var result = emailVerificationService.sendVerificationEmail(command);
        SendEmailResponse response = new SendEmailResponse(
                result.accepted(),
                result.resendAvailableAfterSeconds()
        );

        return ApiResponse.success(response);
    }

    private void validateConditionalFields(SendEmailRequest request) {
        if (request.purpose() == EmailVerificationPurpose.RESET_PASSWORD
                && (request.loginId() == null || request.loginId().isBlank())) {
            throw RequestValidationException.invalidBodyField(
                    "loginId",
                    "RESET_PASSWORD 목적에서는 loginId가 필요합니다."
            );
        }
    }
}
