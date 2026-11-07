package matchuri.backend.domain.group.repository;

public record GroupCandidateVoteCountRow(
        Long candidateId,
        Long voteCount
) {
}
