package matchuri.backend.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMemberTasteProfileRequest(
        @NotBlank(message = "profileVersion은 비어 있을 수 없습니다.")
        @Size(max = 20, message = "profileVersion은 20자를 초과할 수 없습니다.")
        String profileVersion
) {
}
