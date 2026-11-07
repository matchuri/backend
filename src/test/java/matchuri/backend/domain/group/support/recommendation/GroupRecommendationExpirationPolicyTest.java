package matchuri.backend.domain.group.support.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRoom;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupRecommendationExpirationPolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 31, 12, 0);
    private final GroupRecommendationExpirationPolicy policy = new GroupRecommendationExpirationPolicy();

    @Test
    void preparingHasNoVotingStartedAt() {
        GroupRecommendation recommendation = recommendationCreatedAt(NOW.minusMinutes(10));

        assertThat(recommendation.getStartedAt()).isNull();
        assertThat(policy.isExpired(recommendation, NOW)).isFalse();
    }

    @Test
    void openingSetsVotingStartedAt() {
        GroupRecommendation recommendation = recommendationCreatedAt(NOW.minusMinutes(10));
        LocalDateTime votingStartedAt = NOW.minusMinutes(5);

        recommendation.open(votingStartedAt);

        assertThat(recommendation.getStartedAt()).isEqualTo(votingStartedAt);
    }

    @Test
    void expirationUsesCreatedAtEvenWhenVotingStartedRecently() {
        GroupRecommendation recommendation = recommendationCreatedAt(NOW.minusHours(24));
        recommendation.open(NOW.minusMinutes(1));

        assertThat(policy.isExpired(recommendation, NOW)).isTrue();
    }

    private GroupRecommendation recommendationCreatedAt(LocalDateTime createdAt) {
        GroupRecommendation recommendation = GroupRecommendation.preparing(mock(GroupRoom.class));
        ReflectionTestUtils.setField(recommendation, "createdAt", createdAt);
        return recommendation;
    }
}
