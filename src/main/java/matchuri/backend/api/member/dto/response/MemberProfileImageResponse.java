package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MemberProfileImageResponse(
        @Schema(description = "회원 프로필 이미지 연결 ID", example = "15")
        Long profileImageId,

        @Schema(description = "선택한 프리셋 프로필 이미지 ID", example = "1")
        Long presetProfileImageId,

        @Schema(
                description = "현재 프로필 이미지 공개 URL",
                example = "https://asset.matchuri.com/preset-profile/v1-spaghetti.png"
        )
        String imageUrl,

        @Schema(description = "프로필 이미지 수정 시각", example = "2026-08-24T12:30:00")
        LocalDateTime updatedAt
) {
}
