package matchuri.backend.domain.auth.service;

import matchuri.backend.api.auth.dto.request.EmailSendRequest;
import matchuri.backend.api.auth.dto.response.EmailSendResponse;

public interface EmailVerificationService {
    EmailSendResponse sendTxtEmail(EmailSendRequest request);
}
