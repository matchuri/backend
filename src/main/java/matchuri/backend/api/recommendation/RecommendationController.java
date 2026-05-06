package matchuri.backend.api.recommendation;

import java.util.List;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationResponse;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personal-recommendations")
public class RecommendationController {

    @GetMapping
    public ApiResponse<PageResponse<PersonalRecommendationResponse>> getMyPersonalRecommendationList() {

        PageResponse<PersonalRecommendationResponse> response = PageResponse.mock(
                List.of(PersonalRecommendationResponse.mock(),
                        PersonalRecommendationResponse.mock()));

        return ApiResponse.success(response);
    }
}
