package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.member.entity.Member;

public record CreateNicknameGroupInviteRequest(
        @Schema(description = "초대를 보낼 그룹 ID입니다.", example = "3001")
        @NotNull(message = "groupId는 필수입니다.")
        Long groupId,

        @Schema(description = "초대할 회원의 닉네임입니다.", example = "점심탐험가")
        @NotBlank(message = "nickname은 비어 있을 수 없습니다.")
        @Size(max = Member.NICKNAME_MAX_SIZE, message = "nickname은 " + Member.NICKNAME_MAX_SIZE + "자를 초과할 수 없습니다.")
        String nickname
) {
}
