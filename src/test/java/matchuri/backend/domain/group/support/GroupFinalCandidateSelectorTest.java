package matchuri.backend.domain.group.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import org.junit.jupiter.api.Test;

class GroupFinalCandidateSelectorTest {

    private final GroupFinalCandidateSelector selector = new GroupFinalCandidateSelector();

    @Test
    void selectsCandidateWithMostVotes() {
        GroupRecommendationCandidate first = candidate(1L, 1);
        GroupRecommendationCandidate second = candidate(2L, 2);

        GroupRecommendationCandidate selected = selector.select(
                List.of(first, second),
                Map.of(first.getId(), 1, second.getId(), 2)
        );

        assertThat(selected).isSameAs(second);
    }

    @Test
    void breaksVoteTieByHigherRecommendationRank() {
        GroupRecommendationCandidate first = candidate(1L, 1);
        GroupRecommendationCandidate second = candidate(2L, 2);

        GroupRecommendationCandidate selected = selector.select(
                List.of(first, second),
                Map.of(first.getId(), 1, second.getId(), 1)
        );

        assertThat(selected).isSameAs(first);
    }

    @Test
    void selectsHighestRankWhenThereAreNoVotes() {
        GroupRecommendationCandidate first = candidate(1L, 1);
        GroupRecommendationCandidate second = candidate(2L, 2);

        GroupRecommendationCandidate selected = selector.select(List.of(first, second), Map.of());

        assertThat(selected).isSameAs(first);
    }

    @Test
    void rejectsEmptyCandidateList() {
        assertThatThrownBy(() -> selector.select(List.of(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("그룹 추천 후보가 비어 있습니다.");
    }

    private GroupRecommendationCandidate candidate(Long id, int rankNo) {
        GroupRecommendationCandidate candidate = mock(GroupRecommendationCandidate.class);
        when(candidate.getId()).thenReturn(id);
        when(candidate.getRankNo()).thenReturn(rankNo);
        return candidate;
    }
}
