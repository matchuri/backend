package matchuri.backend.api.recommendation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.recommendation.dto.request.CreateGuestPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.CreatePersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.SelectPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.response.GuestPersonalRecommendationResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationCandidateListResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationDetailResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationRequestResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationResponse;
import matchuri.backend.api.recommendation.dto.response.SelectPersonalRecommendationResponse;
import matchuri.backend.domain.recommendation.command.GuestPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.result.GuestPersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationSummaryResult;
import matchuri.backend.domain.recommendation.result.SelectPersonalRecommendationResult;
import matchuri.backend.domain.recommendation.service.RecommendationService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NullMarked
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationApi {

    private final RecommendationService recommendationService;
    private final RecommendationMapper recommendationMapper;

    @Override
    @PostMapping("/guest/recommendations")
    public ApiResponse<GuestPersonalRecommendationResponse> createGuestPersonalRecommendation(
            @Valid @RequestBody CreateGuestPersonalRecommendationRequest request
    ) {
        GuestPersonalRecommendationCommand command = recommendationMapper.toGuestCommand(request);
        GuestPersonalRecommendationResult result = recommendationService.createGuestPersonalRecommendation(command);

        return ApiResponse.success(recommendationMapper.toGuestResponse(result));
    }

    @Override
    @GetMapping("/personal/recommendations")
    public ApiResponse<PageResponse<PersonalRecommendationResponse>> getMyPersonalRecommendationList(
            @Min(0) @RequestParam(defaultValue = "0") Integer page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "20") Integer size
    ) {
        Page<PersonalRecommendationSummaryResult> results =
                recommendationService.getMyPersonalRecommendations(page, size);

        PageResponse<PersonalRecommendationResponse> response =
                PageResponse.of(results, recommendationMapper::toSummaryResponse);

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/personal/recommendations")
    public ApiResponse<PersonalRecommendationRequestResponse> createPersonalRecommendation(
            @Valid @RequestBody CreatePersonalRecommendationRequest request
    ) {
        String contextJson = recommendationMapper.toContextJson(request);
        PersonalRecommendationResult result = recommendationService.createPersonalRecommendation(contextJson);

        return ApiResponse.success(recommendationMapper.toCreateResponse(result));
    }

    @Override
    @GetMapping("/personal/recommendations/{requestId}")
    public ApiResponse<PersonalRecommendationDetailResponse> getPersonalRecommendation(@PathVariable Long requestId) {
        PersonalRecommendationResult result = recommendationService.getPersonalRecommendation(requestId);

        return ApiResponse.success(recommendationMapper.toDetailResponse(result));
    }

    @Override
    @GetMapping("/personal/recommendations/{requestId}/candidates")
    public ApiResponse<PersonalRecommendationCandidateListResponse> getPersonalRecommendationCandidates(
            @PathVariable Long requestId
    ) {
        List<PersonalRecommendationCandidateResult> results =
                recommendationService.getPersonalRecommendationCandidates(requestId);

        return ApiResponse.success(recommendationMapper.toCandidateListResponse(requestId, results));
    }

    @Override
    @PatchMapping("/personal/recommendations/{requestId}")
    public ApiResponse<SelectPersonalRecommendationResponse> selectPersonalRecommendationCandidate(
            @PathVariable Long requestId,
            @Valid @RequestBody SelectPersonalRecommendationRequest request
    ) {
        SelectPersonalRecommendationResult result = recommendationService.selectPersonalRecommendationCandidate(
                requestId,
                request.selectedCandidateId()
        );

        return ApiResponse.success(recommendationMapper.toSelectResponse(result));
    }
}
