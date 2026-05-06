package matchuri.backend.api.recommendation;

import jakarta.validation.Valid;
import java.util.List;
import matchuri.backend.api.recommendation.dto.request.CreatePersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.SelectPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationCandidateListResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationDetailResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationRequestResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationResponse;
import matchuri.backend.api.recommendation.dto.response.SelectPersonalRecommendationResponse;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RecommendationController implements RecommendationApi {

    @Override
    @GetMapping("/personal-recommendations")
    public ApiResponse<PageResponse<PersonalRecommendationResponse>> getMyPersonalRecommendationList() {

        PageResponse<PersonalRecommendationResponse> response = PageResponse.mock(
                List.of(PersonalRecommendationResponse.mock()));

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/personal-recommendation-requests")
    public ApiResponse<PersonalRecommendationRequestResponse> createPersonalRecommendation(
            @Valid @RequestBody CreatePersonalRecommendationRequest request
    ) {
        return ApiResponse.success(PersonalRecommendationRequestResponse.mockCompleted());
    }

    @Override
    @GetMapping("/personal-recommendation-requests/{requestId}")
    public ApiResponse<PersonalRecommendationDetailResponse> getPersonalRecommendation(@PathVariable Long requestId) {
        return ApiResponse.success(PersonalRecommendationDetailResponse.mockSelected());
    }

    @Override
    @GetMapping("/personal-recommendation-requests/{requestId}/candidates")
    public ApiResponse<PersonalRecommendationCandidateListResponse> getPersonalRecommendationCandidates(
            @PathVariable Long requestId
    ) {
        return ApiResponse.success(PersonalRecommendationCandidateListResponse.mock());
    }

    @Override
    @PatchMapping("/personal-recommendation-requests/{requestId}")
    public ApiResponse<SelectPersonalRecommendationResponse> selectPersonalRecommendationCandidate(
            @PathVariable Long requestId,
            @Valid @RequestBody SelectPersonalRecommendationRequest request
    ) {
        return ApiResponse.success(SelectPersonalRecommendationResponse.mockSelected(request.selectedCandidateId()));
    }
}
