package matchuri.backend.api.auth.dto.response;

public record SendEmailResponse(
        long id,
        String email,
        String type
) {
}
