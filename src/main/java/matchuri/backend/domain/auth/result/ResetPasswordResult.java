package matchuri.backend.domain.auth.result;

public record ResetPasswordResult(
        boolean reset
) {
    public static ResetPasswordResult success() {
        return new ResetPasswordResult(true);
    }
}
