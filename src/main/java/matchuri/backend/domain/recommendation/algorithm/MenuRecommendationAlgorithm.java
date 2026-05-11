package matchuri.backend.domain.recommendation.algorithm;

import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationResult;

public interface MenuRecommendationAlgorithm {

    /**
     * 알고리즘을 선택할 때 사용하는 추천 알고리즘 종류를 반환한다.
     *
     * @return 추천 알고리즘 종류
     */
    RecommendationAlgorithmType type();

    /**
     * 같은 알고리즘 종류 안에서 세부 구현을 구분하는 버전을 반환한다.
     *
     * @return 알고리즘 버전
     */
    String version();

    /**
     * 추천 입력 snapshot을 기반으로 메뉴 후보를 계산한다.
     *
     * @param input 추천 대상, 참여자 취향, 메뉴 후보, 맥락 정보를 담은 입력 snapshot
     * @return 점수 계산이 끝난 메뉴 추천 결과
     */
    MenuRecommendationResult recommend(MenuRecommendationInput input);
}
