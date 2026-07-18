package matchuri.backend.domain.recommendation.service;

import java.util.List;
import matchuri.backend.domain.recommendation.command.GuestPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.command.SelectPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationRerollType;
import matchuri.backend.domain.recommendation.result.GuestPersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationSummaryResult;
import matchuri.backend.domain.recommendation.result.SelectPersonalRecommendationResult;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;

public interface RecommendationService {
    PersonalRecommendationResult createPersonalRecommendation(String contextJson);

    PersonalRecommendationResult rerollPersonalRecommendation(
            Long sourcePersonalRecommendationId,
            PersonalRecommendationRerollType rerollType,
            String contextJson
    );

    GuestPersonalRecommendationResult createGuestPersonalRecommendation(GuestPersonalRecommendationCommand command);

    PersonalRecommendationResult getPersonalRecommendation(Long personalRecommendationId);

    List<PersonalRecommendationCandidateResult> getPersonalRecommendationCandidates(Long personalRecommendationId);

    Page<@NonNull PersonalRecommendationSummaryResult> getMyPersonalRecommendations(int page, int size);

    SelectPersonalRecommendationResult selectPersonalRecommendationCandidate(SelectPersonalRecommendationCommand command);
}
