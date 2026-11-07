package matchuri.backend.domain.auth.service;

import matchuri.backend.domain.auth.command.ConfirmEmailVerificationCommand;
import matchuri.backend.domain.auth.command.SendEmailVerificationCommand;
import matchuri.backend.domain.auth.result.ConfirmEmailVerificationResult;
import matchuri.backend.domain.auth.result.SendEmailVerificationResult;

public interface EmailVerificationService {
    SendEmailVerificationResult sendVerificationEmail(SendEmailVerificationCommand command);

    ConfirmEmailVerificationResult confirmVerificationEmail(ConfirmEmailVerificationCommand command);
}
