package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record GroupMemberSummaryResponse(
        @Schema(description = "회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "회원 닉네임입니다.", example = "점심탐험가")
        String nickname,

        @Schema(description = "그룹 내 역할입니다.", example = "OWNER")
        GroupMemberRole role,

        @Schema(description = "그룹 멤버 상태입니다.", example = "ACTIVE")
        GroupMemberStatus status,

        @Schema(description = "그룹 참여 시각입니다.", example = "2026-05-06T12:01:00")
        LocalDateTime joinedAt
) {
    public static GroupMemberSummaryResponse mockOwner() {
        return new GroupMemberSummaryResponse(
                1L,
                "점심탐험가",
                GroupMemberRole.OWNER,
                GroupMemberStatus.ACTIVE,
                LocalDateTime.of(2026, 5, 6, 12, 1)
        );
    }

    public static GroupMemberSummaryResponse mockMember(Long memberId, String nickname) {
        return new GroupMemberSummaryResponse(
                memberId,
                nickname,
                GroupMemberRole.MEMBER,
                GroupMemberStatus.ACTIVE,
                LocalDateTime.of(2026, 5, 6, 12, 2)
        );
    }
}
