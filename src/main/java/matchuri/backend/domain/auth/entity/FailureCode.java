package matchuri.backend.domain.auth.entity;

public enum FailureCode {
    TIMEOUT,
    HTTP_5XX,
    HTTP_ERROR,
    UNEXPECTED_ERROR;

    public boolean isRetryable() {
        return this == TIMEOUT || this == HTTP_5XX;
    }
}
