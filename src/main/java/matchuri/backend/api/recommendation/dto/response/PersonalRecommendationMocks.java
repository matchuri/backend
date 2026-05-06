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
}
