package matchuri.backend.global.exception;

public class BusinessException extends MatchuriException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode, errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
