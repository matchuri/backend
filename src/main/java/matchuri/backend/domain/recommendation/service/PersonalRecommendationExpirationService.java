package matchuri.backend.domain.recommendation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalRecommendationExpirationService {

    static final long EXPIRATION_HOURS = 24;

    private final PersonalRecommendationRepository personalRecommendationRepository;

    @Scheduled(fixedRateString = "PT1H")
    public void expireOpenPersonalRecommendationsOnSchedule() {
        int expiredCount = expireOpenPersonalRecommendations();
        if (expiredCount > 0) {
            log.info("Expired {} open personal recommendations", expiredCount);
        }
    }

    @Transactional
    public int expireOpenPersonalRecommendations() {
        LocalDateTime now = LocalDateTime.now();
        List<PersonalRecommendation> targets = personalRecommendationRepository
                .findByStatusAndSelectedCandidateIsNullAndClosedAtIsNullAndRequestedAtLessThanEqual(
                        PersonalRecommendationStatus.OPEN,
                        expirationThreshold(now)
                );

        targets.forEach(recommendation -> recommendation.expire(now));

        return targets.size();
    }

    public boolean isExpired(PersonalRecommendation recommendation, LocalDateTime now) {
        return recommendation.getStatus() == PersonalRecommendationStatus.OPEN
                && recommendation.getSelectedCandidate() == null
                && !recommendation.getRequestedAt().plusHours(EXPIRATION_HOURS).isAfter(now);
    }

    private LocalDateTime expirationThreshold(LocalDateTime now) {
        return now.minusHours(EXPIRATION_HOURS);
    }
}
