package matchuri.backend.domain.recommendation.result;

import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;

public record PersonalRecommendationCandidateResult(
        Long id,
        Long menuId,
        String menuName,
        String thumbnailUrl,
        int rankNo,
        Double score
) {
    public static PersonalRecommendationCandidateResult from(PersonalRecommendationCandidate candidate) {
        return from(candidate, null);
    }

    public static PersonalRecommendationCandidateResult from(
            PersonalRecommendationCandidate candidate,
            String thumbnailUrl
    ) {
        return new PersonalRecommendationCandidateResult(
                candidate.getId(),
                candidate.getMenuItem().getId(),
                candidate.getMenuItem().getName(),
                thumbnailUrl,
                candidate.getRankNo(),
                candidate.getScore()
        );
    }
}
