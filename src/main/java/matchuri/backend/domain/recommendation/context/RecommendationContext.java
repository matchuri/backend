package matchuri.backend.domain.recommendation.context;

public record RecommendationContext(
        MealTime mealTime
) {
    public static RecommendationContext empty() {
        return new RecommendationContext(null);
    }
}
