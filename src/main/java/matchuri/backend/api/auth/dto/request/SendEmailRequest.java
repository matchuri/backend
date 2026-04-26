package matchuri.backend.api.auth.dto.request;

public record SendEmailRequest(
        String email,
        String type
) {
}
