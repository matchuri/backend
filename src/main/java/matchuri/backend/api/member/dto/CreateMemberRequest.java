package matchuri.backend.api.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.member.entity.Member;

public record CreateMemberRequest(
        @NotBlank(message = "loginId는 비어 있을 수 없습니다.")
        @Size(max = Member.LOGIN_ID_MAX_SIZE, message = "loginId는 50자를 초과할 수 없습니다.")
        @Pattern(regexp = Member.LOGIN_ID_PATTERN, message = "loginId는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다.")
        String loginId,

        @NotBlank(message = "password는 비어 있을 수 없습니다.")
        @Size(min = 8, max = 100, message = "password는 8자 이상 100자 이하여야 합니다.")
        String password
) {
}
