package matchuri.backend.api.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.recommendation.dto.request.CreateGuestPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.CreatePersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.RerollPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.SelectPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.response.GuestPersonalRecommendationCandidateResponse;
import matchuri.backend.api.recommendation.dto.response.GuestPersonalRecommendationResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationCandidateListResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationCandidateResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationDetailResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationRequestResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationResponse;
import matchuri.backend.api.recommendation.dto.response.SelectPersonalRecommendationResponse;
import matchuri.backend.domain.recommendation.command.GuestPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.command.SelectPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.result.GuestPersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.GuestPersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationSummaryResult;
import matchuri.backend.domain.recommendation.result.SelectPersonalRecommendationResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationMapper {

    private final ObjectMapper objectMapper;

    public String toContextJson(CreatePersonalRecommendationRequest request) {
        return toContextJson(request.contextJson());
    }

    public String toContextJson(RerollPersonalRecommendationRequest request) {
        return toContextJson(request.contextJson());
    }

    public GuestPersonalRecommendationCommand toGuestCommand(CreateGuestPersonalRecommendationRequest request) {
        return new GuestPersonalRecommendationCommand(
                request.attributeCategoryIds(),
                request.restrictionIngredientIds(),
                request.dislikedMenuItemIds(),
                toContextJson(request.contextJson())
        );
    }

    public SelectPersonalRecommendationCommand toSelectCommand(
            Long personalRecommendationId,
            SelectPersonalRecommendationRequest request
    ) {
        return new SelectPersonalRecommendationCommand(
                personalRecommendationId,
                request.selectedCandidateId(),
                request.latitude(),
                request.longitude(),
                request.radiusMeters(),
                request.address()
        );
    }

    public GuestPersonalRecommendationResponse toGuestResponse(GuestPersonalRecommendationResult result) {
        return new GuestPersonalRecommendationResponse(
                result.candidates().stream()
                        .map(this::toGuestCandidateResponse)
                        .toList()
        );
    }

    private String toContextJson(Map<String, Object> contextJson) {
        try {
            Map<String, Object> normalizedContextJson = contextJson == null ? Map.of() : contextJson;

            return objectMapper.writeValueAsString(normalizedContextJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("contextJson을 JSON 문자열로 변환할 수 없습니다.", exception);
        }
    }

    public PersonalRecommendationRequestResponse toCreateResponse(PersonalRecommendationResult result) {
        return new PersonalRecommendationRequestResponse(
                result.id(),
                result.status(),
                result.requestedAt(),
                result.closedAt(),
                toCandidateResponses(result.candidates())
        );
    }

    public PersonalRecommendationDetailResponse toDetailResponse(PersonalRecommendationResult result) {
        return new PersonalRecommendationDetailResponse(
                result.id(),
                result.status(),
                result.closedAt(),
                toContextMap(result.contextJson()),
                toCandidateResponses(result.candidates()),
                result.selectedCandidateId()
        );
    }

    public PersonalRecommendationCandidateListResponse toCandidateListResponse(
            Long personalRecommendationId,
            List<PersonalRecommendationCandidateResult> candidates
    ) {
        return new PersonalRecommendationCandidateListResponse(
                personalRecommendationId,
                toCandidateResponses(candidates)
        );
    }

    public PersonalRecommendationResponse toSummaryResponse(PersonalRecommendationSummaryResult result) {
        return new PersonalRecommendationResponse(
                result.id(),
                result.status(),
                result.requestedAt(),
                result.closedAt()
        );
    }

    public SelectPersonalRecommendationResponse toSelectResponse(SelectPersonalRecommendationResult result) {
        return new SelectPersonalRecommendationResponse(
                result.id(),
                result.status(),
                result.selectedCandidateId(),
                result.closedAt(),
                result.updatedAt()
        );
    }

    private List<PersonalRecommendationCandidateResponse> toCandidateResponses(
            List<PersonalRecommendationCandidateResult> candidates
    ) {
        return candidates.stream()
                .map(this::toCandidateResponse)
                .toList();
    }

    private PersonalRecommendationCandidateResponse toCandidateResponse(PersonalRecommendationCandidateResult result) {
        return new PersonalRecommendationCandidateResponse(
                result.id(),
                result.menuId(),
                result.menuName(),
                result.thumbnailUrl(),
                result.rankNo(),
                result.score()
        );
    }

    private GuestPersonalRecommendationCandidateResponse toGuestCandidateResponse(
            GuestPersonalRecommendationCandidateResult result
    ) {
        return new GuestPersonalRecommendationCandidateResponse(
                result.menuId(),
                result.menuName(),
                result.thumbnailUrl(),
                result.rankNo(),
                result.score()
        );
    }

    private Map<String, Object> toContextMap(String contextJson) {
        if (contextJson == null || contextJson.isBlank()) {
            return Map.of();
        }

        try {
            JsonNode contextNode = objectMapper.readTree(contextJson);
            while (contextNode.isTextual()) {
                contextNode = objectMapper.readTree(contextNode.asText());
            }

            return objectMapper.convertValue(contextNode, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 contextJson을 해석할 수 없습니다.", exception);
        }
    }
}
