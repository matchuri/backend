package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMemberBasicInfoRequest(
        @Schema(
                description = "수정할 닉네임입니다. null이면 닉네임을 변경하지 않습니다.",
                example = "점심탐험가",
                nullable = true,
                maxLength = 50
        )
        @Pattern(regexp = "^(?!\\s*$).+", message = "nickname은 비어 있을 수 없습니다.")
        @Size(max = 50, message = "nickname은 50자를 초과할 수 없습니다.")
        String nickname
) {
}
