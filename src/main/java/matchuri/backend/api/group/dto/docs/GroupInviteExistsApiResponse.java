package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupInviteExistsResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "PENDING 그룹 초대 존재 여부 API의 공통 응답 envelope입니다.")
public record GroupInviteExistsApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "PENDING 그룹 초대 존재 여부입니다.")
        GroupInviteExistsResponse data,

        @Schema(description = "실패 정보입니다. 성공 시 null입니다.", nullable = true)
        ErrorResponse error
) {
}
