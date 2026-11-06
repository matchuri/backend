package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record GroupMemberSummaryV2Response(
        @Schema(description = "회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "회원 닉네임입니다.", example = "점심탐험가")
        String nickname,

        @Schema(
                description = "회원 프로필 이미지 공개 URL입니다. 프로필 이미지가 없으면 null입니다.",
                example = "https://asset.matchuri.com/preset-profile/v1-spaghetti.png",
                nullable = true
        )
        String memberProfileImageUrl,

        @Schema(description = "그룹 내 역할입니다.", example = "OWNER")
        GroupMemberRole role,

        @Schema(description = "그룹 멤버 상태입니다.", example = "ACTIVE")
        GroupMemberStatus status,

        @Schema(description = "그룹 참여 시각입니다.", example = "2026-05-06T12:01:00")
        LocalDateTime joinedAt,

        @Schema(description = "현재 로그인한 회원 본인 여부입니다.", example = "true")
        boolean isMe
) {
}
