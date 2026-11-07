package matchuri.backend.domain.recommendation.result;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationHistoryResult(
        Long id,
        PersonalRecommendationStatus status,
        LocalDateTime requestedAt,
        LocalDateTime closedAt,
        Double score,
        String menuName,
        List<String> tags,
        String thumbnailUrl
) {
    public PersonalRecommendationHistoryResult {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public static PersonalRecommendationHistoryResult of(
            PersonalRecommendation recommendation,
            PersonalRecommendationCandidate representativeCandidate,
            List<String> tags,
            String thumbnailUrl
    ) {
        if (representativeCandidate == null) {
            return new PersonalRecommendationHistoryResult(
                    recommendation.getId(),
                    recommendation.getStatus(),
                    recommendation.getRequestedAt(),
                    recommendation.getClosedAt(),
                    null,
                    null,
                    List.of(),
                    null
            );
        }

        return new PersonalRecommendationHistoryResult(
                recommendation.getId(),
                recommendation.getStatus(),
                recommendation.getRequestedAt(),
                recommendation.getClosedAt(),
                representativeCandidate.getScore(),
                representativeCandidate.getMenuItem().getName(),
                tags,
                thumbnailUrl
        );
    }
}
