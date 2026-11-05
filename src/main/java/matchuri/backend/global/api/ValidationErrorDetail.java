package matchuri.backend.global.api;

public record ValidationErrorDetail(
        String source,
        String field,
        String reason
) {
}
