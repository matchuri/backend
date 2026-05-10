package matchuri.backend.domain.recommendation.service;

import java.util.List;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationSummaryResult;
import matchuri.backend.domain.recommendation.result.SelectPersonalRecommendationResult;

public interface RecommendationService {
    PersonalRecommendationResult createPersonalRecommendation(String contextJson);

    PersonalRecommendationResult getPersonalRecommendation(Long personalRecommendationId);

    List<PersonalRecommendationCandidateResult> getPersonalRecommendationCandidates(Long personalRecommendationId);

    List<PersonalRecommendationSummaryResult> getMyPersonalRecommendations();

    SelectPersonalRecommendationResult selectPersonalRecommendationCandidate(
            Long personalRecommendationId,
            Long selectedCandidateId
    );
}
