package matchuri.backend.domain.recommendation.result;

public record GuestPersonalRecommendationCandidateResult(
        Long menuId,
        String menuName,
        String thumbnailUrl,
        Integer rankNo,
        Double score
) {
}
