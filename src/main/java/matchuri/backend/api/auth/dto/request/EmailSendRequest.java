package matchuri.backend.api.auth.dto.request;

public record EmailSendRequest(
        String email,
        String type
) {
}
