package matchuri.backend.domain.recommendation.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import matchuri.backend.domain.recommendation.algorithm.guest.GuestPersonalMenuRecommendationAlgorithmV1;
import matchuri.backend.domain.recommendation.algorithm.group.GroupMenuRecommendationAlgorithmV1;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.personal.PersonalMenuRecommendationAlgorithmV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RecommendationPreferenceBaselineTest {

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("유형 정보 없는 기존 ID 기반 입력은 음식 분류와 온도감 대안을 각각 분모에 포함한다")
    void flatPreferenceIdsScoreHalfWhenOneAlternativePerTypeMatches(MenuRecommendationAlgorithm algorithm) {
        TasteProfileSnapshot participant = participant(List.of(10L, 11L, 20L, 21L));
        var result = algorithm.recommend(input(algorithm, participant, List.of(10L, 20L)));

        assertThat(result.candidates().getFirst().score()).isEqualTo(50.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("유형 정보 없는 기존 ID 기반 입력은 선택한 모든 속성이 일치해야 만점을 받는다")
    void flatPreferenceIdsScoreFullWhenAllSelectedIdsMatch(MenuRecommendationAlgorithm algorithm) {
        TasteProfileSnapshot participant = participant(List.of(10L, 11L, 20L, 21L));
        var result = algorithm.recommend(input(algorithm, participant, List.of(10L, 11L, 20L, 21L)));

        assertThat(result.candidates().getFirst().score()).isEqualTo(100.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("유형 정보 없는 기존 입력의 혼합 선호는 항목별 일치율 4/6으로 계산한다")
    void flatPreferenceIdsScoreMixedPreferencesByIdCount(MenuRecommendationAlgorithm algorithm) {
        TasteProfileSnapshot participant = participant(List.of(10L, 11L, 20L, 21L, 30L, 31L));
        var result = algorithm.recommend(input(algorithm, participant, List.of(10L, 20L, 30L, 31L)));

        assertThat(result.candidates().getFirst().score()).isEqualTo(66.7);
    }

    private static Stream<MenuRecommendationAlgorithm> algorithms() {
        return Stream.of(
                new PersonalMenuRecommendationAlgorithmV1(),
                new GuestPersonalMenuRecommendationAlgorithmV1(),
                new GroupMenuRecommendationAlgorithmV1()
        );
    }

    private TasteProfileSnapshot participant(List<Long> preferences) {
        return new TasteProfileSnapshot(1L, "1", preferences, List.of(), List.of());
    }

    private MenuRecommendationInput input(
            MenuRecommendationAlgorithm algorithm,
            TasteProfileSnapshot participant,
            List<Long> menuAttributes
    ) {
        RecommendationTargetType target = switch (algorithm.type()) {
            case PERSONAL -> RecommendationTargetType.PERSONAL;
            case GUEST_PERSONAL -> RecommendationTargetType.GUEST_PERSONAL;
            case GROUP -> RecommendationTargetType.GROUP;
        };
        return new MenuRecommendationInput(
                target,
                algorithm.type() == RecommendationAlgorithmType.GROUP
                        ? List.of(participant, participant, participant) : List.of(participant),
                List.of(new MenuRecommendationProfile(1L, "M1", "메뉴1", menuAttributes, List.of())),
                null,
                3,
                List.of(),
                List.of(),
                Map.of()
        );
    }
}
