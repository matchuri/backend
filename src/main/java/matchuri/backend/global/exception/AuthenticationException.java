package matchuri.backend.global.exception;

public class AuthenticationException extends MatchuriException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
