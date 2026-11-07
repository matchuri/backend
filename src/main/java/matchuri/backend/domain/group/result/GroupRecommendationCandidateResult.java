package matchuri.backend.domain.group.result;

import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;

public record GroupRecommendationCandidateResult(
        Long candidateId,
        Long menuId,
        String menuName,
        String thumbnailUrl,
        Integer rankNo,
        Double score,
        Integer voteCount
) {
    public static GroupRecommendationCandidateResult from(GroupRecommendationCandidate candidate, int voteCount) {
        return from(candidate, voteCount, null);
    }

    public static GroupRecommendationCandidateResult from(
            GroupRecommendationCandidate candidate,
            int voteCount,
            String thumbnailUrl
    ) {
        return new GroupRecommendationCandidateResult(
                candidate.getId(),
                candidate.getMenuItem().getId(),
                candidate.getMenuItem().getName(),
                thumbnailUrl,
                candidate.getRankNo(),
                candidate.getScore(),
                voteCount
        );
    }
}
