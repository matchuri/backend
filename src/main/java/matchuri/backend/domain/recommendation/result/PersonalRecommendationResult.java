package matchuri.backend.domain.recommendation.result;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationResult(
        Long id,
        PersonalRecommendationStatus status,
        LocalDateTime requestedAt,
        LocalDateTime closedAt,
        String contextJson,
        Long selectedCandidateId,
        List<PersonalRecommendationCandidateResult> candidates
) {
    public static PersonalRecommendationResult of(
            PersonalRecommendation personalRecommendation,
            List<PersonalRecommendationCandidate> candidates
    ) {
        Long selectedCandidateId = personalRecommendation.getSelectedCandidate() == null
                ? null
                : personalRecommendation.getSelectedCandidate().getId();

        return new PersonalRecommendationResult(
                personalRecommendation.getId(),
                personalRecommendation.getStatus(),
                personalRecommendation.getRequestedAt(),
                personalRecommendation.getClosedAt(),
                personalRecommendation.getContextJson(),
                selectedCandidateId,
                candidates.stream()
                        .map(PersonalRecommendationCandidateResult::from)
                        .toList()
        );
    }
}
