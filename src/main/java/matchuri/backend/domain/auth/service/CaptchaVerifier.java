package matchuri.backend.domain.auth.service;

public interface CaptchaVerifier {
    boolean verify(String token, CaptchaPurpose purpose, String clientIp);
}
