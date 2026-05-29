package matchuri.backend.domain.group.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.repository.GroupRecommendationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupRecommendationExpirationService {

    static final long EXPIRATION_HOURS = 24;
    private static final List<GroupRecommendationStatus> EXPIRABLE_STATUSES = List.of(
            GroupRecommendationStatus.PREPARING,
            GroupRecommendationStatus.OPEN
    );

    private final GroupRecommendationRepository groupRecommendationRepository;

    @Scheduled(fixedRateString = "PT1H")
    public void expireActiveGroupRecommendationsOnSchedule() {
        int expiredCount = expireActiveGroupRecommendations();
        if (expiredCount > 0) {
            log.info("Expired {} active group recommendations", expiredCount);
        }
    }

    @Transactional
    public int expireActiveGroupRecommendations() {
        LocalDateTime now = LocalDateTime.now();
        List<GroupRecommendation> targets = groupRecommendationRepository
                .findByStatusInAndEndedAtIsNullAndStartedAtLessThanEqual(
                        EXPIRABLE_STATUSES,
                        expirationThreshold(now)
                );

        targets.forEach(recommendation -> recommendation.expire(now));

        return targets.size();
    }

    public boolean isExpired(GroupRecommendation recommendation, LocalDateTime now) {
        return EXPIRABLE_STATUSES.contains(recommendation.getStatus())
                && recommendation.getEndedAt() == null
                && !recommendation.getStartedAt().plusHours(EXPIRATION_HOURS).isAfter(now);
    }

    private LocalDateTime expirationThreshold(LocalDateTime now) {
        return now.minusHours(EXPIRATION_HOURS);
    }
}
