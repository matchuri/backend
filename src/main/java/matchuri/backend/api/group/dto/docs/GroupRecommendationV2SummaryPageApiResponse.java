package matchuri.backend.api.group.dto.docs;

import matchuri.backend.api.group.dto.response.GroupRecommendationV2SummaryResponse;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.global.api.PageResponse;

public record GroupRecommendationV2SummaryPageApiResponse(
        boolean success,
        PageResponse<GroupRecommendationV2SummaryResponse> data,
        ErrorResponse error
) {
}
