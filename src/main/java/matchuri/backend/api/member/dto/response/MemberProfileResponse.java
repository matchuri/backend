package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberProfileResponse(
        @Schema(
                description = "현재 로그인한 회원 ID입니다.",
                example = "1"
        )
        Long id,

        @Schema(
                description = "현재 로그인한 회원의 로그인 ID 입니다. 소셜 로그인은 null일 수 있습니다.",
                example = "user01"
        )
        String loginId,

        @Schema(
                description = "현재 로그인한 회원 닉네임입니다. 아직 설정하지 않았다면 null일 수 있습니다.",
                example = "점심탐험가",
                nullable = true
        )
        String nickname,

        @Schema(
                description = "현재 로그인한 회원의 소셜 여부입니다.",
                example = "true"
        )
        boolean isSocial,

        @Schema(
                description = "현재 로그인한 회원의 이메일입니다. 레거시 계정이면 null일 수 있습니다.",
                example = "test01@example.com",
                nullable = true
        )
        String email,

        @Schema(
                description = "현재 프로필 이미지 공개 URL입니다. 기존 미설정 회원은 null일 수 있습니다.",
                example = "https://asset.matchuri.com/preset-profile/v1-spaghetti.png",
                nullable = true
        )
        String profileImageUrl
) {
}
