package matchuri.backend.domain.group.support.recommendation;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.repository.GroupRecommendationRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationStatusQueryRow;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupRecommendationExpirationManager {

    private static final List<GroupRecommendationStatus> ACTIVE_RECOMMENDATION_STATUSES = List.of(
            GroupRecommendationStatus.PREPARING,
            GroupRecommendationStatus.OPEN
    );

    private final GroupRecommendationRepository groupRecommendationRepository;
    private final GroupRecommendationExpirationPolicy groupRecommendationExpirationPolicy;

    public boolean hasActiveRecommendation(Long roomId) {
        return groupRecommendationRepository.existsByRoomIdAndStatusInAndCreatedAtAfter(
                roomId,
                ACTIVE_RECOMMENDATION_STATUSES,
                groupRecommendationExpirationPolicy.activeThreshold(LocalDateTime.now())
        );
    }

    public void expireActiveGroupRecommendations(Long roomId, LocalDateTime now) {
        expireActiveGroupRecommendations(List.of(roomId), now);
    }

    public void expireActiveGroupRecommendations(List<Long> roomIds, LocalDateTime now) {
        if (roomIds.isEmpty()) {
            return;
        }

        groupRecommendationRepository
                .findByRoomIdInAndStatusInAndEndedAtIsNullAndCreatedAtLessThanEqual(
                        roomIds,
                        ACTIVE_RECOMMENDATION_STATUSES,
                        groupRecommendationExpirationPolicy.activeThreshold(now)
                )
                .forEach(recommendation -> recommendation.expire(now));
    }

    public boolean expireGroupRecommendationIfNeeded(GroupRecommendation recommendation, LocalDateTime now) {
        if (recommendation.getStatus() == GroupRecommendationStatus.EXPIRED) {
            return true;
        }

        if (!groupRecommendationExpirationPolicy.isExpired(recommendation, now)) {
            return false;
        }

        recommendation.expire(now);
        return true;
    }

    public GroupRecommendationStatus latestRecommendationStatus(Long roomId) {
        return groupRecommendationRepository.findFirstByRoomIdOrderByCreatedAtDescIdDesc(roomId)
                .map(GroupRecommendation::getStatus)
                .orElse(null);
    }

    public Map<Long, GroupRecommendationStatus> latestRecommendationStatuses(Collection<Long> roomIds) {
        return groupRecommendationRepository.findLatestStatusesByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(
                        GroupRecommendationStatusQueryRow::roomId,
                        GroupRecommendationStatusQueryRow::status
                ));
    }
}
