package matchuri.backend.domain.auth.service;

public interface CaptchaService {
    boolean verifyToken(String token, String expectedAction, String clientIp);
}
