package matchuri.backend.domain.recommendation.result;

import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;

public record PersonalRecommendationCandidateResult(
        Long id,
        Long menuId,
        String menuName,
        int rankNo,
        Double score
) {
    public static PersonalRecommendationCandidateResult from(PersonalRecommendationCandidate candidate) {
        return new PersonalRecommendationCandidateResult(
                candidate.getId(),
                candidate.getMenuItem().getId(),
                candidate.getMenuItem().getName(),
                candidate.getRankNo(),
                candidate.getScore()
        );
    }
}
