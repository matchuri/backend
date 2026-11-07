package matchuri.backend.global.exception;

public class AuthorizationException extends MatchuriException {

    public AuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
