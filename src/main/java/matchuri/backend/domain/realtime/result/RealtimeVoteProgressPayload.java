package matchuri.backend.domain.realtime.result;

import matchuri.backend.domain.group.result.GroupVoteProgressResult;

public record RealtimeVoteProgressPayload(
        Integer totalMemberCount,
        Integer votedMemberCount,
        boolean allVoted
) {
    public static RealtimeVoteProgressPayload from(GroupVoteProgressResult progress) {
        boolean allVoted = progress.totalMemberCount() > 0
                && progress.totalMemberCount().equals(progress.votedMemberCount());

        return new RealtimeVoteProgressPayload(
                progress.totalMemberCount(),
                progress.votedMemberCount(),
                allVoted
        );
    }
}
