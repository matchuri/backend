package matchuri.backend.api.member.dto;

public record LoginIdExistsResponse(
        String loginId,
        boolean exists
) {
}
