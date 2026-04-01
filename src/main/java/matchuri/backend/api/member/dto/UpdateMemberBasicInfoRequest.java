package matchuri.backend.api.member.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMemberBasicInfoRequest(
        @Pattern(regexp = "^(?!\\s*$).+", message = "nickname은 비어 있을 수 없습니다.")
        @Size(max = 50, message = "nickname은 50자를 초과할 수 없습니다.")
        String nickname
) {
}
