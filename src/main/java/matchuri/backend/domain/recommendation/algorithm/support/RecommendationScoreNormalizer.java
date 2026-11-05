package matchuri.backend.domain.recommendation.algorithm.support;

public final class RecommendationScoreNormalizer {

    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 100.0;
    private static final double DECIMAL_SCALE = 10.0;

    private RecommendationScoreNormalizer() {
    }

    public static double normalize(double score, double maxPossibleScore) {
        if (score <= 0 || maxPossibleScore <= 0) {
            return MIN_SCORE;
        }

        double normalizedScore = score * MAX_SCORE / maxPossibleScore;
        double clampedScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, normalizedScore));

        return Math.round(clampedScore * DECIMAL_SCALE) / DECIMAL_SCALE;
    }
}
