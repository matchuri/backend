package matchuri.backend.domain.recommendation.algorithm.personal;

import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.support.SingleParticipantMenuRecommendationAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class PersonalMenuRecommendationAlgorithmV1 extends SingleParticipantMenuRecommendationAlgorithm {

    private static final String VERSION = "v1";

    @Override
    public RecommendationAlgorithmType type() {
        return RecommendationAlgorithmType.PERSONAL;
    }

    @Override
    public String version() {
        return VERSION;
    }
}
