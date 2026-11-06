package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupInviteLinkResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "현재 그룹 초대 링크 조회 API의 공통 응답 envelope입니다.")
public record CurrentGroupInviteLinkApiResponse(
        boolean success,

        @Schema(description = "활성 링크가 없으면 null입니다.", nullable = true)
        GroupInviteLinkResponse data,

        ErrorResponse error
) {
}
