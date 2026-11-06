package matchuri.backend.domain.group.support;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import org.springframework.stereotype.Component;

@Component
public class GroupFinalCandidateSelector {

    public GroupRecommendationCandidate select(
            List<GroupRecommendationCandidate> candidates,
            Map<Long, Integer> voteCountsByCandidateId
    ) {
        return candidates.stream()
                .max((left, right) -> {
                    int voteComparison = Integer.compare(
                            voteCountsByCandidateId.getOrDefault(left.getId(), 0),
                            voteCountsByCandidateId.getOrDefault(right.getId(), 0)
                    );

                    if (voteComparison != 0) {
                        return voteComparison;
                    }

                    return Integer.compare(right.getRankNo(), left.getRankNo());
                })
                .orElseThrow(() -> new IllegalArgumentException("그룹 추천 후보가 비어 있습니다."));
    }
}
