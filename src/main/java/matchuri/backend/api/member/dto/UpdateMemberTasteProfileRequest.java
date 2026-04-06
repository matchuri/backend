package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.member.entity.MemberTasteProfile;

public record UpdateMemberTasteProfileRequest(
        @Schema(
                description = "현재 프론트가 저장한 취향 프로필 버전입니다.",
                example = "v1",
                maxLength = MemberTasteProfile.PROFILE_VERSION_MAX_SIZE
        )
        @NotBlank(message = "profileVersion은 비어 있을 수 없습니다.")
        @Size(
                max = MemberTasteProfile.PROFILE_VERSION_MAX_SIZE,
                message = "profileVersion은 " + MemberTasteProfile.PROFILE_VERSION_MAX_SIZE + "자를 초과할 수 없습니다."
        )
        String profileVersion
) {
}
