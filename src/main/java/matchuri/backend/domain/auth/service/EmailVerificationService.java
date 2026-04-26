package matchuri.backend.domain.auth.service;

import matchuri.backend.api.auth.dto.request.SendEmailRequest;
import matchuri.backend.api.auth.dto.response.SendEmailResponse;

public interface EmailVerificationService {
    SendEmailResponse sendTxtEmail(SendEmailRequest request);
}
