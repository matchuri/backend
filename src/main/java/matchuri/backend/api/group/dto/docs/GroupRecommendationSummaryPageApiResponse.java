package matchuri.backend.api.group.dto.docs;

import matchuri.backend.api.group.dto.response.GroupRecommendationSummaryResponse;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.global.api.PageResponse;

public record GroupRecommendationSummaryPageApiResponse(
        boolean success,
        PageResponse<GroupRecommendationSummaryResponse> data,
        ErrorResponse error
) {
}
