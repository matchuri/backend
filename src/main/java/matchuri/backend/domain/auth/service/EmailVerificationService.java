package matchuri.backend.domain.auth.service;

import matchuri.backend.api.auth.dto.request.EmailVerificationRequest;

public interface EmailVerificationService {
    void sendTxtEmail(EmailVerificationRequest request);
}
