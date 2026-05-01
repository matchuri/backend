package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 응답입니다.")
public record UpdateMemberPasswordResponse(
        @Schema(description = "비밀번호 변경 성공 여부입니다.", example = "true")
        boolean passwordChanged
) {
}
