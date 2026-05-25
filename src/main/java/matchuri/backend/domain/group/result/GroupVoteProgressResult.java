package matchuri.backend.domain.group.result;

public record GroupVoteProgressResult(
        Integer totalMemberCount,
        Integer votedMemberCount
) {
}
