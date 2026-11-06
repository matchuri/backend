package matchuri.backend.domain.group.support.recommendation;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupRecommendationOpeningManager {

    private final GroupRecommendationCandidateGenerator candidateGenerator;
    private final GroupRecommendationHistoryReader historyReader;
    private final GroupRecommendationResultAssembler resultAssembler;

    public List<GroupRecommendationCandidateResult> open(GroupRoom room, GroupRecommendation recommendation) {
        List<GroupRecommendationCandidate> candidates = candidateGenerator.generateCandidatesForRecommendation(
                room,
                recommendation,
                null,
                historyReader.recentlySkippedMenuIds(room.getId())
        );
        recommendation.open(LocalDateTime.now());
        return resultAssembler.toCandidateResults(candidates, 0);
    }
}
