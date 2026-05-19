package matchuri.backend.api.group.dto.docs;

import matchuri.backend.api.group.dto.response.RespondGroupInviteResponse;
import matchuri.backend.global.api.ErrorResponse;

public record RespondGroupInviteApiResponse(
        boolean success,
        RespondGroupInviteResponse data,
        ErrorResponse error
) {
}
