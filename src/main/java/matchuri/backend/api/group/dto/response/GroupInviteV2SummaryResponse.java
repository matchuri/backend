package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupInviteV2SummaryResponse(
        @Schema(description = "그룹 초대 PK ID입니다.", example = "501")
        Long id,

        @Schema(description = "초대 대상 그룹 이름입니다.", example = "맛집 탐방 모임")
        String groupName,

        @Schema(
                description = "초대한 회원의 프로필 이미지 공개 URL입니다.",
                example = "https://asset.matchuri.com/preset-profile/v1-spaghetti.png",
                nullable = true
        )
        String requestMemberProfileImageUrl,

        @Schema(description = "초대한 회원의 닉네임입니다.", example = "나는야 임영웅")
        String requestMemberNickname
) {
}
