package matchuri.backend.domain.recommendation.repository;

public record PersonalRecommendationCandidateQueryRow(
        Long id,
        Long menuId,
        String menuName,
        String thumbnailObjectKey,
        Integer rankNo,
        Double score
) {
}
