package matchuri.backend.global.api;


import java.util.List;
import matchuri.backend.global.exception.ErrorCode;

public record ErrorResponse(
        int status,
        String code,
        String message,
        List<ValidationErrorDetail> details
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, Object... args) {
        return new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.format(args),
                List.of()
        );
    }

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<ValidationErrorDetail> details) {
        return new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                details
        );
    }
}
