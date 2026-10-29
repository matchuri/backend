package matchuri.backend.recommendation.algorithm.personal;

import java.util.List;
import java.util.Map;
import matchuri.backend.catalog.entity.CategoryType;
import matchuri.backend.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.recommendation.algorithm.support.RecommendationPreferenceMatchCalculator;
import matchuri.backend.recommendation.algorithm.support.SingleParticipantMenuRecommendationAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class PersonalMenuRecommendationAlgorithmV1 extends SingleParticipantMenuRecommendationAlgorithm {

    private static final String VERSION = "v1.2";

    @Override
    public RecommendationAlgorithmType type() {
        return RecommendationAlgorithmType.PERSONAL;
    }

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    protected RecommendationPreferenceMatchCalculator.PreferenceMatch calculatePreferenceMatch(
            List<Long> menuAttributeIds,
            List<Long> preferredAttributeIds,
            Map<Long, CategoryType> categoryTypes
    ) {
        return RecommendationPreferenceMatchCalculator.calculateByCategoryType(
                menuAttributeIds, preferredAttributeIds, categoryTypes);
    }
}
