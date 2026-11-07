package matchuri.backend.domain.group.repository;

public record GroupRecommendationCandidateQueryRow(
        Long candidateId,
        Long menuId,
        String menuName,
        String thumbnailObjectKey,
        Integer rankNo,
        Double score,
        Long voteCount
) {
}
