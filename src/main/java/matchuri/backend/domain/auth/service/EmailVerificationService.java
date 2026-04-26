package matchuri.backend.domain.auth.service;

import matchuri.backend.domain.auth.command.SendEmailVerificationCommand;
import matchuri.backend.domain.auth.result.SendEmailVerificationResult;

public interface EmailVerificationService {
    SendEmailVerificationResult sendVerificationEmail(SendEmailVerificationCommand command);
}
