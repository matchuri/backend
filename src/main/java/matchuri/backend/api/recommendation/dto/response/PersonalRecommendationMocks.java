package matchuri.backend.api.recommendation.dto.response;

import java.util.List;
import java.util.Map;

final class PersonalRecommendationMocks {

    private PersonalRecommendationMocks() {
    }

    static List<PersonalRecommendationCandidateResponse> candidates() {
        return List.of(
                PersonalRecommendationCandidateResponse.mockBibimbap(),
                PersonalRecommendationCandidateResponse.mockPorkCutlet(),
                PersonalRecommendationCandidateResponse.mockRiceNoodle()
        );
    }

    static Map<String, Object> contextJson() {
        return Map.of(
                "mealTime", "LUNCH",
                "budgetLevel", 2,
                "mood", "가볍지만 든든한 점심"
        );
    }

    static Map<String, Object> resultJson() {
        return Map.of(
                "summary", "가볍고 매콤한 점심 후보를 우선 추천했습니다.",
                "algorithmVersion", "mock-v1",
                "profileSnapshotVersion", "v1",
                "candidateCount", 3
        );
    }
}
