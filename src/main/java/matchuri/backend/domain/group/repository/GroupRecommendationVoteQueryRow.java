package matchuri.backend.domain.group.repository;

public record GroupRecommendationVoteQueryRow(
        Long memberId,
        Long candidateId
) {
}
