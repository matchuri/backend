package matchuri.backend.domain.recommendation.algorithm.guest;

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

class GuestPersonalMenuRecommendationAlgorithmV1Test {

    private final GuestPersonalMenuRecommendationAlgorithmV1 algorithm = new GuestPersonalMenuRecommendationAlgorithmV1();

    @Test
    @DisplayName("비회원 취향 입력으로 제한 재료와 비선호 메뉴를 제외한 후보를 반환한다")
    void recommendGuestPersonalCandidates() {
        TasteProfileSnapshot participant = new TasteProfileSnapshot(
                null,
                "guest",
                List.of(10L),
                List.of(100L),
                List.of(3L)
        );
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.GUEST_PERSONAL,
                List.of(participant),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L), List.of()),
                        new MenuRecommendationProfile(2L, "M2", "메뉴2", List.of(10L), List.of(100L)),
                        new MenuRecommendationProfile(3L, "M3", "메뉴3", List.of(10L), List.of()),
                        new MenuRecommendationProfile(4L, "M4", "메뉴4", List.of(), List.of())
                ),
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(),
                Map.of()
        );

        MenuRecommendationResult result = algorithm.recommend(input);

        assertThat(result.algorithmType()).isEqualTo(RecommendationAlgorithmType.GUEST_PERSONAL);
        assertThat(result.algorithmVersion()).isEqualTo("v1");
        assertThat(result.candidates())
                .extracting(candidate -> candidate.menuId())
                .containsExactly(1L, 4L);
    }
}
