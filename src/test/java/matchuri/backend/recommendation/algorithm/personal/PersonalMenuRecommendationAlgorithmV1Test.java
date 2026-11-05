package matchuri.backend.recommendation.algorithm.personal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import matchuri.backend.catalog.entity.CategoryType;
import matchuri.backend.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.recommendation.algorithm.RecommendationTargetType;
import matchuri.backend.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.recommendation.algorithm.input.RecommendationContextSnapshot;
import matchuri.backend.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.recommendation.algorithm.output.MenuRecommendationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PersonalMenuRecommendationAlgorithmV1Test {

    private final PersonalMenuRecommendationAlgorithmV1 algorithm = new PersonalMenuRecommendationAlgorithmV1();

    @Test
    @DisplayName("제한 재료, 비선호 메뉴, 최근 선택 메뉴를 제외하고 점수순 후보를 반환한다")
    void recommendFiltersAndScoresPersonalCandidates() {
        TasteProfileSnapshot participant = new TasteProfileSnapshot(
                1L,
                "1",
                List.of(10L, 20L),
                List.of(100L),
                List.of(3L)
        );
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(participant),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L, 20L), List.of(200L)),
                        new MenuRecommendationProfile(2L, "M2", "메뉴2", List.of(10L), List.of(100L)),
                        new MenuRecommendationProfile(3L, "M3", "메뉴3", List.of(10L, 20L), List.of(200L)),
                        new MenuRecommendationProfile(4L, "M4", "메뉴4", List.of(30L), List.of(200L)),
                        new MenuRecommendationProfile(5L, "M5", "메뉴5", List.of(20L), List.of(200L))
                ),
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(5L),
                List.of(),
                Map.of(10L, 2L, 30L, 1L)
        );

        MenuRecommendationResult result = algorithm.recommend(input);

        assertThat(result.algorithmType()).isEqualTo(RecommendationAlgorithmType.PERSONAL);
        assertThat(result.algorithmVersion()).isEqualTo("v1.3");
        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(1L, 4L);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.rankNo())
                .containsExactly(1, 2);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(100.0, 16.7);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("취향이 모두 일치하면 이력 속성이 여러 개여도 최종 점수와 메타는 동일한 100점이다")
    void recommendKeepsBoundedFinalScoreInCandidateAndMetadata() {
        TasteProfileSnapshot participant = new TasteProfileSnapshot(
                1L,
                "1",
                List.of(10L),
                List.of(),
                List.of()
        );
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(participant),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L, 20L, 30L), List.of())
                ),
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(),
                List.of(),
                Map.of(20L, 2L, 30L, 2L)
        );

        MenuRecommendationResult result = algorithm.recommend(input);

        assertThat(result.candidates().getFirst().score()).isEqualTo(100.0);
        assertConsistentFinalScores(result);
    }

    @ParameterizedTest
    @MethodSource("historyScenarios")
    @DisplayName("이력은 취향의 부족한 점수에만 보너스를 더하고 첫 이력이나 이력 수 증가로 점수를 낮추지 않는다")
    void recommendOnlyAddsHistoryBonusToPartialPreferenceMatch(
            Map<Long, Long> history,
            double expectedScore
    ) {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L, 20L, 30L),
                List.of(new MenuRecommendationProfile(1L, "M1", "세 유형 중 둘 일치", List.of(10L, 20L), List.of())),
                preferenceCategoryTypes(),
                history
        ));

        assertThat(result.candidates().getFirst().score()).isEqualTo(expectedScore);
        assertThat(result.candidates().getFirst().score()).isGreaterThanOrEqualTo(66.7);
        assertConsistentFinalScores(result);
    }

    @ParameterizedTest
    @MethodSource("fullMatchHistoryScenarios")
    @DisplayName("모든 취향 유형이 일치하면 이력이 없거나 추가되어도 100점을 유지한다")
    void recommendKeepsFullPreferenceMatchAtOneHundredWithAnyHistory(Map<Long, Long> history) {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L, 20L, 30L),
                List.of(new MenuRecommendationProfile(1L, "M1", "모든 유형 일치", List.of(10L, 20L, 30L), List.of())),
                preferenceCategoryTypes(),
                history
        ));

        assertThat(result.candidates().getFirst().score()).isEqualTo(100.0);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("이력 점유율은 메뉴 속성 ID가 중복되어도 같은 속성의 빈도를 한 번만 계산한다")
    void recommendCountsDistinctMenuAttributesInHistoryShare() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L),
                List.of(new MenuRecommendationProfile(1L, "M1", "중복 속성", List.of(20L, 20L, 20L), List.of())),
                preferenceCategoryTypes(),
                Map.of(20L, 1L, 90L, 1L)
        ));

        assertThat(result.candidates().getFirst().score()).isEqualTo(25.0);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("취향이 불일치하면 메뉴 속성이 많고 모든 이력이 일치해도 보너스는 최대 50점이다")
    void recommendBoundsHistoryBonusForMenusWithManyAttributes() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L),
                List.of(new MenuRecommendationProfile(1L, "M1", "여러 이력 속성 일치", List.of(20L, 30L, 40L, 50L),
                        List.of())),
                preferenceCategoryTypes(),
                Map.of(20L, 2L, 30L, 2L, 40L, 2L, 50L, 2L)
        ));

        assertThat(result.candidates().getFirst().score()).isEqualTo(50.0);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("선호가 없으면 이력 점유율만 100점 척도로 계산하는 기존 정책을 유지한다")
    void recommendKeepsHistoryOnlyScoringWithoutPreferences() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "이력 25퍼센트", List.of(20L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "이력 75퍼센트", List.of(20L, 30L), List.of())
                ),
                preferenceCategoryTypes(),
                Map.of(20L, 1L, 30L, 2L, 90L, 1L)
        ));

        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(2L, 1L);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(75.0, 25.0);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("이력 원점수가 높았던 메뉴도 최종 점수가 낮으면 더 높은 응답 점수의 메뉴 뒤에 정렬한다")
    void recommendRanksByFinalScoreInsteadOfLegacyHistoryWeight() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L, 20L),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "취향 불일치 이력 절반", List.of(30L, 31L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "취향 절반 이력 사분의 일", List.of(10L), List.of())
                ),
                preferenceCategoryTypes(),
                Map.of(10L, 1L, 30L, 1L, 31L, 1L, 90L, 1L)
        ));

        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(2L, 1L);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(56.3, 25.0);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("최종 점수를 소수 첫째 자리로 반올림한 뒤 동점이면 메뉴 ID 오름차순으로 정렬한다")
    void recommendBreaksRoundedFinalScoreTiesByMenuId() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L),
                List.of(
                        new MenuRecommendationProfile(2L, "M2", "반올림 전 0.52점", List.of(92L), List.of()),
                        new MenuRecommendationProfile(1L, "M1", "반올림 전 0.515점", List.of(91L), List.of())
                ),
                preferenceCategoryTypes(),
                Map.of(91L, 103L, 92L, 104L, 90L, 9793L)
        ));

        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(1L, 2L);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(0.5, 0.5);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("후보 수 제한을 적용한다")
    void recommendLimitsCandidateCount() {
        TasteProfileSnapshot participant = new TasteProfileSnapshot(
                1L,
                "1",
                List.of(10L),
                List.of(),
                List.of()
        );
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(participant),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "메뉴2", List.of(10L), List.of()),
                        new MenuRecommendationProfile(3L, "M3", "메뉴3", List.of(10L), List.of())
                ),
                RecommendationContextSnapshot.of("{}"),
                2,
                List.of(),
                List.of(),
                Map.of()
        );

        MenuRecommendationResult result = algorithm.recommend(input);

        assertThat(result.candidates()).hasSize(2);
    }

    @Test
    @DisplayName("최근 SKIP 메뉴를 제외한다")
    void recommendFiltersRecentlySkippedMenus() {
        TasteProfileSnapshot participant = new TasteProfileSnapshot(
                1L,
                "1",
                List.of(10L),
                List.of(),
                List.of()
        );
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(participant),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "메뉴2", List.of(10L), List.of()),
                        new MenuRecommendationProfile(3L, "M3", "메뉴3", List.of(10L), List.of())
                ),
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(),
                List.of(1L, 3L),
                Map.of()
        );

        MenuRecommendationResult result = algorithm.recommend(input);

        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(2L);
    }

    @ParameterizedTest
    @EnumSource(CategoryType.class)
    @DisplayName("개인 추천은 모든 유형에서 선택한 대안 중 하나 이상이 일치하면 한 평가 단위만 충족한다")
    void recommendMatchesAlternativesOncePerCategoryType(CategoryType categoryType) {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L, 11L),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "단일 일치", List.of(10L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "복수 일치", List.of(10L, 11L), List.of())
                ),
                Map.of(10L, categoryType, 11L, categoryType)
        ));

        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(100.0, 100.0);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.scoreBreakdown().get("categoryMatchingCount"))
                .containsExactly(1L, 1L);
    }

    @ParameterizedTest
    @EnumSource(CategoryType.class)
    @DisplayName("개인 추천은 같은 유형이어도 선택하지 않은 속성 ID에는 일치 점수를 주지 않는다")
    void recommendRequiresSelectedAttributeIdWithinCategoryType(CategoryType categoryType) {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L, 11L),
                List.of(new MenuRecommendationProfile(1L, "M1", "다른 속성", List.of(12L), List.of())),
                Map.of(10L, categoryType, 11L, categoryType, 12L, categoryType)
        ));

        assertThat(result.candidates().getFirst().score()).isZero();
        assertThat(result.candidates().getFirst().scoreBreakdown().get("categoryMatchingCount"))
                .isEqualTo(0L);
    }

    @Test
    @DisplayName("선택한 세 유형 중 두 유형만 일치하면 66.7점이며 세 유형을 충족한 메뉴보다 뒤에 정렬한다")
    void recommendScoresAndRanksByMatchedCategoryTypesWithoutHistory() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L, 11L, 20L, 21L, 30L),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "두 유형 일치", List.of(10L, 11L, 20L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "세 유형 일치", List.of(11L, 21L, 30L), List.of())
                ),
                preferenceCategoryTypes()
        ));

        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(2L, 1L);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(100.0, 66.7);
        assertThat(result.candidates().get(1).scoreBreakdown().get("categoryMatchingCount"))
                .isEqualTo(2L);
    }

    @Test
    @DisplayName("같은 유형에서 선호 대안을 더 선택해도 기존 메뉴의 일치 점수는 낮아지지 않는다")
    void recommendKeepsScoreWhenAddingAlternativesWithinSelectedTypes() {
        List<MenuRecommendationProfile> menus = List.of(
                new MenuRecommendationProfile(1L, "M1", "두 유형 일치", List.of(10L, 20L), List.of())
        );
        MenuRecommendationResult original = algorithm.recommend(preferenceInput(
                List.of(10L, 20L, 30L), menus, preferenceCategoryTypes()
        ));
        MenuRecommendationResult expanded = algorithm.recommend(preferenceInput(
                List.of(10L, 11L, 20L, 21L, 30L, 31L), menus, preferenceCategoryTypes()
        ));

        assertThat(original.candidates().getFirst().score()).isEqualTo(66.7);
        assertThat(expanded.candidates().getFirst().score()).isEqualTo(original.candidates().getFirst().score());
    }

    @Test
    @DisplayName("선택하지 않은 유형과 메뉴의 추가 속성은 선호 점수의 분모와 감점에 포함하지 않는다")
    void recommendIgnoresUnselectedTypesAndAdditionalMenuAttributes() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "선호만 일치", List.of(10L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "추가 속성", List.of(10L, 11L, 20L, 30L, 40L, 50L),
                                List.of())
                ),
                preferenceCategoryTypes()
        ));

        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(100.0, 100.0);
    }

    @Test
    @DisplayName("선호가 없고 이력도 없으면 유형 정보가 있어도 개인 추천 점수는 0점이다")
    void recommendScoresZeroWithoutPreferencesOrHistory() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(),
                List.of(new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L, 20L, 30L), List.of())),
                preferenceCategoryTypes()
        ));

        assertThat(result.candidates().getFirst().score()).isZero();
    }

    @Test
    @DisplayName("유형을 모르는 속성 ID는 각각 독립 평가 단위로 계산하며 알려진 유형의 OR 평가와 함께 적용한다")
    void recommendCountsUnknownAttributeIdsIndividually() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L, 11L, 90L, 91L),
                List.of(new MenuRecommendationProfile(1L, "M1", "알려진 유형과 미지 속성 일치", List.of(10L, 90L),
                        List.of())),
                preferenceCategoryTypes()
        ));

        assertThat(result.candidates().getFirst().score()).isEqualTo(66.7);
        assertThat(result.candidates().getFirst().scoreBreakdown().get("categoryMatchingCount"))
                .isEqualTo(2L);
    }

    private MenuRecommendationInput preferenceInput(
            List<Long> preferences,
            List<MenuRecommendationProfile> menus,
            Map<Long, CategoryType> categoryTypes
    ) {
        return preferenceInput(preferences, menus, categoryTypes, Map.of());
    }

    private MenuRecommendationInput preferenceInput(
            List<Long> preferences,
            List<MenuRecommendationProfile> menus,
            Map<Long, CategoryType> categoryTypes,
            Map<Long, Long> history
    ) {
        return new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(new TasteProfileSnapshot(1L, "1", preferences, List.of(), List.of())),
                menus,
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(),
                List.of(),
                history,
                categoryTypes
        );
    }

    private static Stream<Arguments> historyScenarios() {
        return Stream.of(
                Arguments.of(Map.<Long, Long>of(), 66.7),
                Arguments.of(Map.of(10L, 0L, 90L, 0L), 66.7),
                Arguments.of(Map.of(90L, 1L), 66.7),
                Arguments.of(Map.of(10L, 1L, 90L, 1L), 75.0),
                Arguments.of(Map.of(10L, 100L, 90L, 100L), 75.0)
        );
    }

    private static Stream<Map<Long, Long>> fullMatchHistoryScenarios() {
        return Stream.of(
                Map.of(),
                Map.of(90L, 1L),
                Map.of(10L, 1L, 90L, 1L),
                Map.of(10L, 100L, 90L, 100L)
        );
    }

    private void assertConsistentFinalScores(MenuRecommendationResult result) {
        assertThat(result.candidates()).allSatisfy(candidate -> {
            assertThat(candidate.score()).isBetween(0.0, 100.0);
            assertThat(candidate.scoreBreakdown().get("rawScore")).isEqualTo(candidate.score());
            assertThat(candidate.scoreBreakdown().get("normalizedScore")).isEqualTo(candidate.score());
        });
    }

    private Map<Long, CategoryType> preferenceCategoryTypes() {
        return Map.of(
                10L, CategoryType.FLAVOR,
                11L, CategoryType.FLAVOR,
                20L, CategoryType.COOKING_METHOD,
                21L, CategoryType.COOKING_METHOD,
                30L, CategoryType.TEXTURE,
                31L, CategoryType.TEXTURE,
                40L, CategoryType.FOOD_CATEGORY,
                50L, CategoryType.TEMPERATURE
        );
    }
}
