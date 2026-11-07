package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record WithdrawMemberResponse(
        @Schema(description = "탈퇴 처리된 회원 ID입니다.", example = "1")
        Long id,

        @Schema(description = "탈퇴 후 회원 상태입니다.", example = "DELETED")
        String status
) {
}
