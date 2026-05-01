package matchuri.backend.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.member.entity.Member;

@Schema(
        name = "UpdateMemberPasswordRequest",
        example = """
                {
                  "currentPassword": "P@ssw0rd!",
                  "newPassword": "N3wP@ssw0rd!"
                }
                """
)
public record UpdateMemberPasswordRequest(
        @Schema(
                description = "현재 사용 중인 비밀번호입니다.",
                example = "P@ssw0rd!",
                minLength = Member.PASSWORD_MIN_SIZE,
                maxLength = Member.PASSWORD_MAX_SIZE
        )
        @NotBlank(message = "currentPassword는 비어 있을 수 없습니다.")
        @Size(
                min = Member.PASSWORD_MIN_SIZE,
                max = Member.PASSWORD_MAX_SIZE,
                message = "currentPassword는 " + Member.PASSWORD_MIN_SIZE + "자 이상 "
                        + Member.PASSWORD_MAX_SIZE + "자 이하여야 합니다."
        )
        String currentPassword,

        @Schema(
                description = "새 비밀번호입니다. 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.",
                example = "N3wP@ssw0rd!",
                minLength = Member.PASSWORD_MIN_SIZE,
                maxLength = Member.PASSWORD_MAX_SIZE
        )
        @NotBlank(message = "newPassword는 비어 있을 수 없습니다.")
        @Size(
                min = Member.PASSWORD_MIN_SIZE,
                max = Member.PASSWORD_MAX_SIZE,
                message = "newPassword는 " + Member.PASSWORD_MIN_SIZE + "자 이상 "
                        + Member.PASSWORD_MAX_SIZE + "자 이하여야 합니다."
        )
        @Pattern(
                regexp = Member.PASSWORD_PATTERN,
                message = "비밀번호는 " + Member.PASSWORD_MIN_SIZE + "자 이상이며 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
        )
        String newPassword
) {
}
