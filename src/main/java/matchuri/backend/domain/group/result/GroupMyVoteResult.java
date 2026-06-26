package matchuri.backend.domain.group.result;

public record GroupMyVoteResult(
        boolean voted,
        Long candidateId
) {
}
