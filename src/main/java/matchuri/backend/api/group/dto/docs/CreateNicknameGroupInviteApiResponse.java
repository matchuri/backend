package matchuri.backend.api.group.dto.docs;

import matchuri.backend.api.group.dto.response.CreateNicknameGroupInviteResponse;
import matchuri.backend.global.api.ErrorResponse;

public record CreateNicknameGroupInviteApiResponse(
        boolean success,
        CreateNicknameGroupInviteResponse data,
        ErrorResponse error
) {
}
