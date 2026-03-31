package matchuri.backend.global.exception;

import matchuri.backend.domain.common.CommonErrorCode;
import matchuri.backend.global.api.ValidationErrorDetail;

public class RequestValidationException extends MatchuriException {

    private final ValidationErrorDetail detail;

    private RequestValidationException(ErrorCode errorCode, ValidationErrorDetail detail) {
        super(errorCode);
        this.detail = detail;
    }

    public static RequestValidationException invalidPathVariable(String field, String reason) {
        return new RequestValidationException(
                CommonErrorCode.INVALID_PATH_VARIABLE,
                new ValidationErrorDetail("PATH", field, reason)
        );
    }

    public ValidationErrorDetail getDetail() {
        return detail;
    }
}
