package matchuri.backend.domain.group.entity;

public enum GroupRecommendationStatus {
    OPEN,
    FINALIZED,
    REROLLED_WITH_SKIP,
    REROLLED_WITHOUT_SKIP,
    CANCELED,
    EXPIRED,
    FAILED
}
