package matchuri.backend.global.exception;

import lombok.Getter;

@Getter
public abstract class MatchuriException extends RuntimeException {

    private final ErrorCode errorCode;

    protected MatchuriException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected MatchuriException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
