package matchuri.backend.domain.recommendation.service;

import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import org.springframework.stereotype.Service;

@Service
public class PersonalRecommendationExpirationService {

    static final long EXPIRATION_HOURS = 24;

    public boolean isExpired(PersonalRecommendation recommendation, LocalDateTime now) {
        return recommendation.getStatus() == PersonalRecommendationStatus.OPEN
                && recommendation.getSelectedCandidate() == null
                && !recommendation.getRequestedAt().plusHours(EXPIRATION_HOURS).isAfter(now);
    }

    public LocalDateTime activeThreshold(LocalDateTime now) {
        return now.minusHours(EXPIRATION_HOURS);
    }
}
