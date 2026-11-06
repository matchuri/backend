package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupInviteExistsResponse(
        @Schema(description = "현재 회원에게 만료되지 않은 PENDING 그룹 초대가 존재하는지 여부입니다.", example = "true")
        boolean exists
) {
}
