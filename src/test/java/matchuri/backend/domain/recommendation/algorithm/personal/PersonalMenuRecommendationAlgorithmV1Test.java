package matchuri.backend.domain.recommendation.algorithm.personal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.RecommendationTargetType;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.RecommendationContextSnapshot;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
        assertThat(result.algorithmVersion()).isEqualTo("v1.5");
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
    @DisplayName("동점 후보는 앞서 고른 전체 메뉴와 조리 방식 및 음식 분류가 겹치지 않는 순서로 고른다")
    void recommendDiversifiesTiesAgainstAllSelectedMenus() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L),
                List.of(
                        menu(4L, "구이 일식", 10L, 23L, 41L),
                        menu(5L, "구이 양식", 10L, 23L, 42L),
                        menu(2L, "튀김과 면 양식", 10L, 21L, 22L, 42L),
                        menu(8L, "볶음 양식", 10L, 24L, 42L),
                        menu(6L, "볶음 한식", 10L, 24L, 40L),
                        menu(7L, "볶음 일식", 10L, 24L, 41L),
                        menu(3L, "구이 한식", 10L, 23L, 40L),
                        menu(1L, "국물과 면 한식", 10L, 20L, 21L, 40L)
                ),
                Map.of(
                        10L, CategoryType.FLAVOR,
                        20L, CategoryType.COOKING_METHOD,
                        21L, CategoryType.COOKING_METHOD,
                        22L, CategoryType.COOKING_METHOD,
                        23L, CategoryType.COOKING_METHOD,
                        24L, CategoryType.COOKING_METHOD,
                        40L, CategoryType.FOOD_CATEGORY,
                        41L, CategoryType.FOOD_CATEGORY,
                        42L, CategoryType.FOOD_CATEGORY
                )
        ));

        assertThat(result.candidates()).extracting(candidate -> candidate.menuId()).containsExactly(1L, 4L, 8L);
        assertThat(result.candidates()).extracting(candidate -> candidate.rankNo()).containsExactly(1, 2, 3);
        assertThat(result.candidates()).extracting(candidate -> candidate.score()).containsExactly(100.0, 100.0, 100.0);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("2위는 소수 첫째 자리까지 반올림한 최고점 후보 사이에서 다양성을 반영한다")
    void recommendUsesSecondCandidateDiversityWithinHighestRemainingRoundedScore() {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L),
                List.of(
                        menu(1L, "100점 국물", 10L, 20L),
                        menu(3L, "반올림 전 0.52점 국물", 20L, 91L),
                        menu(4L, "반올림 전 0.515점 구이", 21L, 92L),
                        menu(2L, "0점 튀김", 22L)
                ),
                Map.of(
                        10L, CategoryType.FLAVOR,
                        20L, CategoryType.COOKING_METHOD,
                        21L, CategoryType.COOKING_METHOD,
                        22L, CategoryType.COOKING_METHOD
                ),
                Map.of(91L, 104L, 92L, 103L, 99L, 9793L),
                2
        ));

        assertThat(result.candidates()).extracting(candidate -> candidate.menuId()).containsExactly(1L, 4L);
        assertThat(result.candidates()).extracting(candidate -> candidate.score()).containsExactly(100.0, 0.5);
        assertConsistentFinalScores(result);
    }

    @ParameterizedTest
    @CsvSource({
            "839, 639, 3, 4, 63.9",
            "839, 638, 3, 3, 83.9",
            "1000, 800, 3, 4, 80.0",
            "839, 639, 4, 3, 83.9"
    })
    @DisplayName("후보 세 개를 요청하면 기존 3위와의 점수 차이가 허용 범위 안인 대안으로 3위를 바꾼다")
    void recommendReplacesOnlyThirdCandidateWithinInclusiveScoreGap(
            long thirdHistoryFrequency, long alternativeHistoryFrequency, int limit, long thirdMenuId, double thirdScore
    ) {
        Map<Long, Long> history = Map.of(
                91L, 1000L - thirdHistoryFrequency,
                92L, thirdHistoryFrequency - alternativeHistoryFrequency,
                93L, alternativeHistoryFrequency
        );
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(),
                List.of(
                        menu(1L, "100점 국물", 20L, 91L, 92L, 93L),
                        menu(2L, "100점 국물 대안", 20L, 91L, 92L, 93L),
                        menu(3L, "기존 3위 국물", 20L, 92L, 93L),
                        menu(4L, "낮은 점수 구이", 21L, 93L)
                ),
                preferenceCategoryTypes(),
                history,
                limit
        ));

        assertThat(result.candidates()).hasSize(limit);
        assertThat(result.candidates().subList(0, 3)).extracting(candidate -> candidate.menuId())
                .containsExactly(1L, 2L, thirdMenuId);
        assertThat(result.candidates().subList(0, 3)).extracting(candidate -> candidate.score())
                .containsExactly(100.0, 100.0, thirdScore);
        assertConsistentFinalScores(result);
    }

    @Test
    @DisplayName("3위 대안은 양쪽 조리 방식과 제외 조건을 확인하고 점수, 음식 분류, 메뉴 ID 순으로 고른다")
    void recommendChoosesHighestEligibleThirdAlternativeThenCuisineAndMenuId() {
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(new TasteProfileSnapshot(1L, "1", List.of(), List.of(), List.of())),
                List.of(
                        menu(1L, "100점 한식 국물", 20L, 40L, 91L, 92L, 93L, 94L),
                        menu(2L, "100점 한식 구이", 21L, 40L, 91L, 92L, 93L, 94L),
                        menu(3L, "100점 한식 국물 대안", 20L, 40L, 91L, 92L, 93L, 94L),
                        menu(4L, "94점 일식 튀김", 22L, 41L, 91L),
                        menu(5L, "99점 일식 국물과 튀김", 20L, 22L, 41L, 91L, 92L, 93L),
                        menu(6L, "99점 일식 구이와 튀김", 21L, 22L, 41L, 91L, 92L, 93L),
                        menu(7L, "99점 일식 조리 방식 미확인", 41L, 91L, 92L, 93L),
                        menu(8L, "95점 한식 튀김", 22L, 40L, 91L, 92L),
                        menu(9L, "95점 일식 튀김", 22L, 41L, 91L, 92L),
                        menu(10L, "95점 양식 튀김", 22L, 42L, 91L, 92L),
                        menu(11L, "최근 SKIP한 99점 일식 튀김", 22L, 41L, 91L, 92L, 93L)
                ),
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(),
                List.of(11L),
                Map.of(91L, 940L, 92L, 10L, 93L, 40L, 94L, 10L),
                Map.of(
                        20L, CategoryType.COOKING_METHOD,
                        21L, CategoryType.COOKING_METHOD,
                        22L, CategoryType.COOKING_METHOD,
                        40L, CategoryType.FOOD_CATEGORY,
                        41L, CategoryType.FOOD_CATEGORY,
                        42L, CategoryType.FOOD_CATEGORY
                )
        );

        MenuRecommendationResult result = algorithm.recommend(input);

        assertThat(result.candidates()).extracting(candidate -> candidate.menuId()).containsExactly(1L, 2L, 9L);
        assertThat(result.candidates()).extracting(candidate -> candidate.rankNo()).containsExactly(1, 2, 3);
        assertThat(result.candidates()).extracting(candidate -> candidate.score()).containsExactly(100.0, 100.0, 95.0);
        assertConsistentFinalScores(result);
    }

    @ParameterizedTest
    @MethodSource("thirdCandidateCookingPreferenceScenarios")
    @DisplayName("기존 3위가 선호 조리 방식에 맞으면 대안도 선택한 조리 방식에 맞아야 한다")
    void recommendPreservesPreferredCookingWhenReplacingThirdCandidate(
            List<Long> preferences, long thirdMenuId, double thirdScore
    ) {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                preferences,
                List.of(
                        menu(1L, "한식 국물", 10L, 20L, 30L, 31L, 40L, 50L),
                        menu(2L, "한식 국물 대안", 10L, 20L, 30L, 31L, 40L, 50L),
                        menu(3L, "기존 3위 한식 국물", 10L, 20L, 30L, 31L, 40L, 50L),
                        menu(4L, "80점 한식 구이", 10L, 21L, 30L, 40L, 50L)
                ),
                preferenceCategoryTypes()
        ));

        assertThat(result.candidates()).extracting(candidate -> candidate.menuId()).containsExactly(1L, 2L, thirdMenuId);
        assertThat(result.candidates()).extracting(candidate -> candidate.score()).containsExactly(100.0, 100.0, thirdScore);
    }

    @ParameterizedTest
    @CsvSource({"20, 21, 22", "20, 21, 99", "99, 20, 20", "20, 99, 20"})
    @DisplayName("조리 방식이 이미 다양하거나 어느 후보의 조리 방식이 미확인이면 기존 3위를 유지한다")
    void recommendKeepsThirdCandidateWhenCookingOverlapCannotBeConfirmed(
            long firstCookingTag, long secondCookingTag, long thirdCookingTag
    ) {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(),
                List.of(
                        menu(1L, "100점 첫 번째 후보", firstCookingTag, 91L, 92L),
                        menu(2L, "100점 두 번째 후보", secondCookingTag, 91L, 92L),
                        menu(3L, "100점 세 번째 후보", thirdCookingTag, 91L, 92L),
                        menu(4L, "95점 새 조리 방식", 23L, 91L)
                ),
                Map.of(
                        20L, CategoryType.COOKING_METHOD,
                        21L, CategoryType.COOKING_METHOD,
                        22L, CategoryType.COOKING_METHOD,
                        23L, CategoryType.COOKING_METHOD
                ),
                Map.of(91L, 95L, 92L, 5L)
        ));

        assertThat(result.candidates()).extracting(candidate -> candidate.menuId()).containsExactly(1L, 2L, 3L);
        assertThat(result.candidates()).extracting(candidate -> candidate.score()).containsExactly(100.0, 100.0, 100.0);
    }

    @ParameterizedTest
    @MethodSource("diversityTagScenarios")
    @DisplayName("조리 방식을 비교할 수 있는 후보에만 다양성 우선권을 준다")
    void recommendUsesMenuIdWhenCookingDiversityCannotBeDetermined(
            List<Long> firstMenuAttributes, List<Long> expectedMenuIds
    ) {
        MenuRecommendationResult result = algorithm.recommend(preferenceInput(
                List.of(10L),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "첫 메뉴", firstMenuAttributes, List.of()),
                        menu(2L, "조리 방식 미확인", 10L, 99L),
                        menu(3L, "국물", 10L, 20L),
                        menu(4L, "구이", 10L, 21L)
                ),
                preferenceCategoryTypes()
        ));

        assertThat(result.candidates()).extracting(candidate -> candidate.menuId())
                .containsExactlyElementsOf(expectedMenuIds);
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
        return preferenceInput(preferences, menus, categoryTypes, history, 3);
    }

    private MenuRecommendationInput preferenceInput(
            List<Long> preferences,
            List<MenuRecommendationProfile> menus,
            Map<Long, CategoryType> categoryTypes,
            Map<Long, Long> history,
            int candidateLimit
    ) {
        return new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(new TasteProfileSnapshot(1L, "1", preferences, List.of(), List.of())),
                menus,
                RecommendationContextSnapshot.of("{}"),
                candidateLimit,
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

    private static Stream<Arguments> diversityTagScenarios() {
        return Stream.of(
                Arguments.of(List.of(10L), List.of(1L, 2L, 3L)),
                Arguments.of(List.of(10L, 20L), List.of(1L, 4L, 2L))
        );
    }

    private static Stream<Arguments> thirdCandidateCookingPreferenceScenarios() {
        // 대안은 두 경우 모두 80점이다. 첫 경우는 조리 방식, 두 번째는 식감 선호가 불일치한다.
        return Stream.of(
                Arguments.of(List.of(10L, 20L, 30L, 40L, 50L), 3L, 100.0),
                Arguments.of(List.of(10L, 20L, 21L, 31L, 40L, 50L), 4L, 80.0)
        );
    }

    private MenuRecommendationProfile menu(long id, String name, Long... attributes) {
        return new MenuRecommendationProfile(id, "M" + id, name, List.of(attributes), List.of());
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
