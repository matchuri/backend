package matchuri.backend.domain.recommendation.algorithm.group;

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

class GroupMenuRecommendationAlgorithmV1Test {

    private final GroupMenuRecommendationAlgorithmV1 algorithm = new GroupMenuRecommendationAlgorithmV1();

    @Test
    @DisplayName("그룹 추천 응답 점수는 0에서 100 사이로 정규화하고 원점수는 메타에 남긴다")
    void recommendNormalizesGroupCandidateScore() {
        TasteProfileSnapshot firstParticipant = new TasteProfileSnapshot(
                1L,
                "1",
                List.of(10L),
                List.of(),
                List.of()
        );
        TasteProfileSnapshot secondParticipant = new TasteProfileSnapshot(
                2L,
                "2",
                List.of(10L),
                List.of(),
                List.of()
        );
        TasteProfileSnapshot thirdParticipant = new TasteProfileSnapshot(
                3L,
                "3",
                List.of(10L),
                List.of(),
                List.of()
        );
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.GROUP,
                List.of(firstParticipant, secondParticipant, thirdParticipant),
                List.of(
                        new MenuRecommendationProfile(1L, "M1", "메뉴1", List.of(10L), List.of())
                ),
                RecommendationContextSnapshot.of("{}"),
                3,
                List.of(),
                List.of(),
                Map.of()
        );

        MenuRecommendationResult result = algorithm.recommend(input);

        assertThat(result.algorithmType()).isEqualTo(RecommendationAlgorithmType.GROUP);
        assertThat(result.algorithmVersion()).isEqualTo("v1");
        assertThat(result.candidates().getFirst().score()).isEqualTo(100.0);
        assertThat(result.candidates().getFirst().scoreBreakdown().get("rawScore")).isEqualTo(150.0);
    }
}
