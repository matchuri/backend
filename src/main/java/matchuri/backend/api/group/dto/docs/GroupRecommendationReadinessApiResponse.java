package matchuri.backend.api.group.dto.docs;

import matchuri.backend.api.group.dto.response.GroupRecommendationReadinessResponse;
import matchuri.backend.global.api.ErrorResponse;

public record GroupRecommendationReadinessApiResponse(
        boolean success,
        GroupRecommendationReadinessResponse data,
        ErrorResponse error
) {
}
