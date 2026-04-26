package matchuri.backend.api.auth.dto.response;

public record EmailSendResponse(
        long id,
        String email,
        String type
) {
}
