package matchuri.backend.api.member.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMemberRequest(
        @Email(message = "email 형식이 올바르지 않습니다.")
        @Size(max = 150, message = "email은 150자를 초과할 수 없습니다.")
        String email,

        @Valid
        UpdateMemberTasteProfileRequest memberTasteProfile
) {
}
