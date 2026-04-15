package matchuri.backend.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.member.entity.Member;

public record UpdateMemberBasicInfoRequest(
        @Schema(
                description = "수정할 닉네임입니다. null이면 닉네임을 변경하지 않습니다.",
                example = "점심탐험가",
                nullable = true,
                maxLength = Member.NICKNAME_MAX_SIZE
        )
        @Pattern(regexp = "^(?!\\s*$).+", message = "nickname은 비어 있을 수 없습니다.")
        @Size(max = Member.NICKNAME_MAX_SIZE, message = "nickname은 " + Member.NICKNAME_MAX_SIZE + "자를 초과할 수 없습니다.")
        String nickname
) {
}
