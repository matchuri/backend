package matchuri.backend.recommendation.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import matchuri.backend.catalog.entity.CategoryType;
import matchuri.backend.recommendation.algorithm.guest.GuestPersonalMenuRecommendationAlgorithmV1;
import matchuri.backend.recommendation.algorithm.group.GroupMenuRecommendationAlgorithmV1;
import matchuri.backend.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.recommendation.algorithm.personal.PersonalMenuRecommendationAlgorithmV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RecommendationExclusivePreferenceTest {

    private static final Map<Long, CategoryType> CATEGORY_TYPES = Map.ofEntries(
            Map.entry(10L, CategoryType.FOOD_CATEGORY),
            Map.entry(11L, CategoryType.FOOD_CATEGORY),
            Map.entry(12L, CategoryType.FOOD_CATEGORY),
            Map.entry(20L, CategoryType.TEMPERATURE),
            Map.entry(21L, CategoryType.TEMPERATURE),
            Map.entry(22L, CategoryType.TEMPERATURE),
            Map.entry(30L, CategoryType.FLAVOR),
            Map.entry(31L, CategoryType.FLAVOR),
            Map.entry(40L, CategoryType.COOKING_METHOD),
            Map.entry(41L, CategoryType.COOKING_METHOD),
            Map.entry(50L, CategoryType.TEXTURE),
            Map.entry(51L, CategoryType.TEXTURE)
    );

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("음식 분류와 온도감은 선택한 대안이 각각 하나만 일치해도 50점 대신 100점을 받는다")
    void oneAlternativePerExclusiveTypeScoresFull(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(10L, 11L, 20L, 21L), List.of(10L, 20L), 100.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("단일 음식 분류와 온도감 선호는 기존 만점을 유지한다")
    void singlePreferencePerExclusiveTypeIsUnchanged(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(10L, 20L), List.of(10L, 20L), 100.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("선택하지 않은 음식 분류와 온도감은 같은 유형이어도 점수를 받지 않는다")
    void unselectedAlternativesDoNotMatch(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(10L, 11L, 20L, 21L), List.of(12L, 22L), 0.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("음식 분류만 일치하고 온도감은 일치하지 않으면 두 평가 단위 중 하나만 점수를 받는다")
    void exclusiveTypesAreIndependent(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(10L, 11L, 20L, 21L), List.of(10L, 22L), 50.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("음식 분류와 온도감의 대안 및 일반 선호가 모두 충족되면 66.7점 대신 100점을 받는다")
    void mixedPreferencesScoreByCollapsedUnits(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(10L, 11L, 20L, 21L, 30L, 31L), List.of(10L, 20L, 30L, 31L), 100.0);
    }

    @ParameterizedTest
    @MethodSource("guestAndGroupAlgorithms")
    @DisplayName("비회원과 그룹은 맛과 조리법과 식감의 복수 선호를 계속 개별 항목으로 계산한다")
    void nonExclusiveTypesRemainIndividualPreferences(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(30L, 31L, 40L, 41L, 50L, 51L), List.of(30L, 40L, 50L), 50.0);
    }

    @ParameterizedTest
    @MethodSource("guestAndGroupAlgorithms")
    @DisplayName("비회원과 그룹의 혼합 입력에서도 맛과 조리법과 식감은 각 선택 항목의 가중치를 유지한다")
    void mixedNonExclusivePreferencesRetainIndividualWeight(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(10L, 11L, 20L, 21L, 30L, 31L, 40L, 41L, 50L, 51L),
                List.of(10L, 20L, 30L, 40L, 50L), 62.5);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("음식 분류만 선택하면 온도감 평가 단위를 분모에 추가하지 않는다")
    void foodCategoryOnlyHasOnePreferenceUnit(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(10L, 11L), List.of(10L), 100.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("온도감만 선택하면 음식 분류 평가 단위를 분모에 추가하지 않는다")
    void temperatureOnlyHasOnePreferenceUnit(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(20L, 21L), List.of(20L), 100.0);
    }

    @Test
    @DisplayName("그룹은 구성원별 일치율을 평균내고 비선호 1명당 원점수 25점 감점을 유지한다")
    void groupKeepsParticipantWeightAndDislikedPenalty() {
        TasteProfileSnapshot first = new TasteProfileSnapshot(1L, "1", List.of(10L, 11L, 20L, 21L),
                List.of(), List.of());
        TasteProfileSnapshot second = new TasteProfileSnapshot(2L, "2", List.of(30L),
                List.of(), List.of(1L));
        var result = new GroupMenuRecommendationAlgorithmV1().recommend(new MenuRecommendationInput(
                RecommendationTargetType.GROUP, List.of(first, second),
                List.of(new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L, 20L), List.of())),
                null, 3, List.of(), List.of(), Map.of(), CATEGORY_TYPES));

        var candidate = result.candidates().getFirst();
        assertThat(candidate.score()).isEqualTo(25.0);
        assertThat(candidate.scoreBreakdown().get("preferenceScore")).isEqualTo(50.0);
        assertThat(candidate.scoreBreakdown().get("dislikedPenalty")).isEqualTo(25.0);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("같은 배타적 유형의 여러 메뉴 속성이 일치해도 평가 단위는 한 번만 계산한다")
    void multipleMatchesWithinExclusiveTypeDoNotMultiplyScore(MenuRecommendationAlgorithm algorithm) {
        var result = algorithm.recommend(input(algorithm, List.of(10L, 11L, 20L, 21L),
                List.of(10L, 11L, 20L, 21L)));
        var candidate = result.candidates().getFirst();

        assertThat(candidate.score()).isEqualTo(100.0);
        String matchingCountKey = algorithm.type() == RecommendationAlgorithmType.GROUP
                ? "preferenceMatchingCount" : "categoryMatchingCount";
        assertThat(candidate.scoreBreakdown().get(matchingCountKey))
                .isEqualTo(algorithm.type() == RecommendationAlgorithmType.GROUP ? 6L : 2L);
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    @DisplayName("선호가 없는 입력은 배타적 유형 정보가 있어도 0점을 반환한다")
    void emptyPreferencesScoreZero(MenuRecommendationAlgorithm algorithm) {
        assertScore(algorithm, List.of(), List.of(10L, 20L), 0.0);
    }

    private static Stream<MenuRecommendationAlgorithm> algorithms() {
        return Stream.of(
                new PersonalMenuRecommendationAlgorithmV1(),
                new GuestPersonalMenuRecommendationAlgorithmV1(),
                new GroupMenuRecommendationAlgorithmV1()
        );
    }

    private static Stream<MenuRecommendationAlgorithm> guestAndGroupAlgorithms() {
        return Stream.of(
                new GuestPersonalMenuRecommendationAlgorithmV1(),
                new GroupMenuRecommendationAlgorithmV1()
        );
    }

    private void assertScore(
            MenuRecommendationAlgorithm algorithm,
            List<Long> preferences,
            List<Long> menuAttributes,
            double expectedScore
    ) {
        var result = algorithm.recommend(input(algorithm, preferences, menuAttributes));
        assertThat(result.candidates().getFirst().score()).isEqualTo(expectedScore);
    }

    private MenuRecommendationInput input(
            MenuRecommendationAlgorithm algorithm,
            List<Long> preferences,
            List<Long> menuAttributes
    ) {
        TasteProfileSnapshot participant = new TasteProfileSnapshot(1L, "1", preferences, List.of(), List.of());
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
                Map.of(),
                CATEGORY_TYPES
        );
    }
}
