package matchuri.backend.api.recommendation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationHistoryResponse;
import matchuri.backend.api.recommendation.mapper.RecommendationMapper;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationHistoryResult;
import matchuri.backend.domain.recommendation.service.RecommendationService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NullMarked
@RestController
@RequestMapping("/api/v2/personal/recommendations")
@RequiredArgsConstructor
public class RecommendationV2Controller implements RecommendationV2Api {

    private final RecommendationService recommendationService;
    private final RecommendationMapper recommendationMapper;

    @Override
    @GetMapping
    public ApiResponse<PageResponse<PersonalRecommendationHistoryResponse>> getMyPersonalRecommendationList(
            @AuthenticatedMemberId Long memberId,
            @Min(0) @RequestParam(defaultValue = "0") Integer page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "20") Integer size
    ) {
        Page<PersonalRecommendationHistoryResult> results =
                recommendationService.getMyPersonalRecommendationHistories(memberId, page, size);

        return ApiResponse.success(PageResponse.of(results, recommendationMapper::toHistoryResponse));
    }
}
