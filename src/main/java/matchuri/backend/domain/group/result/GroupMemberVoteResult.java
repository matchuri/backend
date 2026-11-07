package matchuri.backend.domain.group.result;

import matchuri.backend.domain.group.entity.GroupMemberRole;

public record GroupMemberVoteResult(
        Long memberId,
        String nickname,
        GroupMemberRole role,
        boolean isMe,
        boolean voted,
        Long candidateId
) {
}
