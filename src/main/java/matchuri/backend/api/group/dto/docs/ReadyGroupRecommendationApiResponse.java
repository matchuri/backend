package matchuri.backend.api.group.dto.docs;

import matchuri.backend.api.group.dto.response.ReadyGroupRecommendationResponse;
import matchuri.backend.global.api.ErrorResponse;

public record ReadyGroupRecommendationApiResponse(
        boolean success,
        ReadyGroupRecommendationResponse data,
        ErrorResponse error
) {
}
