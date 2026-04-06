package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WithdrawMemberResponse(
        @Schema(description = "탈퇴 처리된 회원 ID입니다.", example = "1")
        Long id,

        @Schema(description = "탈퇴 후 회원 상태입니다. 현재 단계에서는 INACTIVE를 반환합니다.", example = "INACTIVE")
        String status
) {
}
