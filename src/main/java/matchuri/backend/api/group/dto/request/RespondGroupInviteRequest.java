package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import matchuri.backend.domain.group.entity.GroupInviteResponseType;

public record RespondGroupInviteRequest(
        @Schema(description = "초대 응답 타입입니다. ACCEPT=수락, DECLINE=거절입니다.", example = "ACCEPT")
        @NotNull(message = "responseType은 필수입니다.")
        GroupInviteResponseType responseType
) {
}
