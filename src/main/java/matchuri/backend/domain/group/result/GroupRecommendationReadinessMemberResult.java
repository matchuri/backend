package matchuri.backend.domain.group.result;

import matchuri.backend.domain.group.entity.GroupMemberRole;

public record GroupRecommendationReadinessMemberResult(
        Long memberId,
        String nickname,
        GroupMemberRole role,
        boolean ready
) {
}
