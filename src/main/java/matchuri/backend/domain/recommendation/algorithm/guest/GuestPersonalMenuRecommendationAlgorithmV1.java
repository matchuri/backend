package matchuri.backend.domain.recommendation.algorithm.guest;

import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.support.SingleParticipantMenuRecommendationAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class GuestPersonalMenuRecommendationAlgorithmV1 extends SingleParticipantMenuRecommendationAlgorithm {

    private static final String VERSION = "v1";

    @Override
    public RecommendationAlgorithmType type() {
        return RecommendationAlgorithmType.GUEST_PERSONAL;
    }

    @Override
    public String version() {
        return VERSION;
    }
}
