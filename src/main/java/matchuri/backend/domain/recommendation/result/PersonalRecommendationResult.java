package matchuri.backend.domain.recommendation.result;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
        return of(personalRecommendation, candidates, Map.of());
    }

    public static PersonalRecommendationResult of(
            PersonalRecommendation personalRecommendation,
            List<PersonalRecommendationCandidate> candidates,
            Map<Long, String> thumbnailUrlsByMenuId
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
                        .map(candidate -> PersonalRecommendationCandidateResult.from(
                                candidate,
                                thumbnailUrlsByMenuId.get(candidate.getMenuItem().getId())
                        ))
                        .toList()
        );
    }
}
