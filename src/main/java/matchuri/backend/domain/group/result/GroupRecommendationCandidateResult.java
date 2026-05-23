package matchuri.backend.domain.group.result;

import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;

public record GroupRecommendationCandidateResult(
        Long candidateId,
        Long menuId,
        String menuName,
        Integer rankNo,
        Double score,
        Integer voteCount
) {
    public static GroupRecommendationCandidateResult from(GroupRecommendationCandidate candidate, int voteCount) {
        return new GroupRecommendationCandidateResult(
                candidate.getId(),
                candidate.getMenuItem().getId(),
                candidate.getMenuItem().getName(),
                candidate.getRankNo(),
                candidate.getScore(),
                voteCount
        );
    }
}
