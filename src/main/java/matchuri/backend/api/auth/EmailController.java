package matchuri.backend.api.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.ConfirmEmailRequest;
import matchuri.backend.api.auth.dto.request.SendEmailRequest;
import matchuri.backend.api.auth.dto.response.ConfirmEmailResponse;
import matchuri.backend.api.auth.dto.response.SendEmailResponse;
import matchuri.backend.domain.auth.command.ConfirmEmailVerificationCommand;
import matchuri.backend.domain.auth.command.SendEmailVerificationCommand;
import matchuri.backend.domain.auth.service.EmailVerificationService;
import matchuri.backend.global.api.ApiResponse;
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

    @Override
    @PostMapping("/email/confirm")
    public ApiResponse<ConfirmEmailResponse> confirmVerificationEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        var command = new ConfirmEmailVerificationCommand(
                request.email(),
                request.purpose(),
                request.loginId(),
                request.code()
        );
        var result = emailVerificationService.confirmVerificationEmail(command);
        ConfirmEmailResponse response = new ConfirmEmailResponse(
                result.verified(),
                result.emailVerificationToken(),
                result.expiresIn()
        );

        return ApiResponse.success(response);
    }
}
