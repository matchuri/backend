package matchuri.backend.domain.recommendation.result;

public record GuestPersonalRecommendationCandidateResult(
        Long menuId,
        String menuName,
        Integer rankNo,
        Double score
) {
}
