package matchuri.backend.domain.recommendation.service;

import java.util.List;
import matchuri.backend.domain.recommendation.command.GuestPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.command.SelectPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationRerollType;
import matchuri.backend.domain.recommendation.result.GuestPersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationHistoryResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationHomeResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationSummaryResult;
import matchuri.backend.domain.recommendation.result.SelectPersonalRecommendationResult;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;

public interface RecommendationService {
    PersonalRecommendationHomeResult getHomeRecommendations(Long memberId);

    PersonalRecommendationResult createPersonalRecommendation(Long memberId, String contextJson);

    PersonalRecommendationResult rerollPersonalRecommendation(Long memberId, Long sourcePersonalRecommendationId, PersonalRecommendationRerollType rerollType, String contextJson);

    GuestPersonalRecommendationResult createGuestPersonalRecommendation(GuestPersonalRecommendationCommand command);

    PersonalRecommendationResult getPersonalRecommendation(Long memberId, Long personalRecommendationId);

    List<PersonalRecommendationCandidateResult> getPersonalRecommendationCandidates(Long memberId, Long personalRecommendationId);

    Page<@NonNull PersonalRecommendationSummaryResult> getMyPersonalRecommendations(Long memberId, int page, int size);

    Page<@NonNull PersonalRecommendationHistoryResult> getMyPersonalRecommendationHistories(
            Long memberId,
            int page,
            int size
    );

    SelectPersonalRecommendationResult selectPersonalRecommendationCandidate(Long memberId, SelectPersonalRecommendationCommand command);
}
