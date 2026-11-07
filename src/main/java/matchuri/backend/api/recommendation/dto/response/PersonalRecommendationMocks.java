package matchuri.backend.api.recommendation.dto.response;

import java.util.List;

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

    static String contextJson() {
        return "{\"latitude\":37.498095,\"longitude\":127.027610,\"radiusMeters\":1000,\"address\":\"서울 강남구 테헤란로 123\"}";
    }
}
