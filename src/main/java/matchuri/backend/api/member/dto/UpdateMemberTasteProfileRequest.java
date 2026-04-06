package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMemberTasteProfileRequest(
        @Schema(
                description = "현재 프론트가 저장한 취향 프로필 버전입니다.",
                example = "v1",
                maxLength = 20
        )
        @NotBlank(message = "profileVersion은 비어 있을 수 없습니다.")
        @Size(max = 20, message = "profileVersion은 20자를 초과할 수 없습니다.")
        String profileVersion
) {
}
