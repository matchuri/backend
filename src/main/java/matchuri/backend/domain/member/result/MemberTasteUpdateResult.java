package matchuri.backend.domain.member.result;

public record MemberTasteUpdateResult(
        MemberTasteProfileSummaryResult profile,
        Long openPersonalRecommendationId
) {
}
