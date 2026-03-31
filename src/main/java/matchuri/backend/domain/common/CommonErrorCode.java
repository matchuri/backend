package matchuri.backend.domain.common;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INVALID_PATH_VARIABLE(HttpStatus.BAD_REQUEST, "경로 변수 형식이 올바르지 않습니다. {0}"),
    INVALID_QUERY_PARAMETER(HttpStatus.BAD_REQUEST, "쿼리 파라미터 형식이 올바르지 않습니다. {0}"),
    INVALID_BODY_FIELD(HttpStatus.BAD_REQUEST, "요청 바디 필드가 올바르지 않습니다. {0}"),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 연동 처리 중 오류가 발생했습니다. {0}"),
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "처리 시간이 초과되었습니다. {0}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "COMMON_";
    }

}

