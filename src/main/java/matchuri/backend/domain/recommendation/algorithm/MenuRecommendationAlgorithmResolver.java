package matchuri.backend.domain.recommendation.algorithm;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuRecommendationAlgorithmResolver {

    private final List<MenuRecommendationAlgorithm> algorithms;

    /**
     * 요청한 알고리즘 종류를 처리할 구현체를 찾는다.
     *
     * @param type 추천 알고리즘 종류
     * @return 추천 알고리즘 구현체
     */
    public MenuRecommendationAlgorithm resolve(RecommendationAlgorithmType type) {
        return algorithms.stream()
                .filter(algorithm -> algorithm.type() == type)
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        RecommendationAlgorithmErrorCode.NOT_FOUND,
                        type
                ));
    }
}
