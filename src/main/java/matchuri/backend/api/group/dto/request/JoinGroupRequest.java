package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinGroupRequest(
        @Schema(description = "그룹 초대 코드입니다.", example = "LUNCH42")
        @NotBlank(message = "inviteCode는 비어 있을 수 없습니다.")
        @Size(max = 32, message = "inviteCode는 32자를 초과할 수 없습니다.")
        String inviteCode
) {
}
