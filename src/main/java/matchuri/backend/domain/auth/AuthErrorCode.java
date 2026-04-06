package matchuri.backend.domain.auth;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 인증 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    LOGOUT_FAILED(HttpStatus.BAD_REQUEST, "로그아웃 처리에 실패했습니다."),
    OAUTH2_PROVIDER_REJECTED(HttpStatus.UNAUTHORIZED, "소셜 로그인 제공자가 인증을 거절했습니다."),
    OAUTH2_PROVIDER_USERINFO_MISSING(HttpStatus.UNAUTHORIZED, "소셜 로그인 사용자 식별 정보가 누락되었습니다."),
    OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 제공자입니다."),
    OAUTH2_PROCESSING_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인 처리에 실패했습니다."),
    OAUTH2_EXCHANGE_CODE_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 소셜 로그인 교환 코드입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "AUTH_";
    }
}
