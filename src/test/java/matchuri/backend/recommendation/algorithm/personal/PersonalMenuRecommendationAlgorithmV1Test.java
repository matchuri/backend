package matchuri.backend.recommendation.algorithm.personal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
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
import org.junit.jupiter.params.provider.EnumSource;

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
        assertThat(result.algorithmVersion()).isEqualTo("v1.2");
        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(1L, 4L);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.rankNo())
                .containsExactly(1, 2);
        assertThat(result.candidates())
                .extracting(candidate -> candidate.score())
                .containsExactly(83.3, 16.7);
    }

    @Test
    @DisplayName("개인 추천 응답 점수는 0에서 100 사이로 정규화하고 원점수는 메타에 남긴다")
    void recommendNormalizesPersonalCandidateScore() {
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
        assertThat(result.candidates().getFirst().scoreBreakdown().get("rawScore")).isEqualTo(150.0);
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
        return new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(new TasteProfileSnapshot(1L, "1", preferences, List.of(), List.of())),
                menus,
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(),
                List.of(),
                Map.of(),
                categoryTypes
        );
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
