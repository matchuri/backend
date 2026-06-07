package matchuri.backend.domain.recommendation.algorithm.personal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.RecommendationTargetType;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.RecommendationContextSnapshot;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        assertThat(result.algorithmVersion()).isEqualTo("v1");
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
}
