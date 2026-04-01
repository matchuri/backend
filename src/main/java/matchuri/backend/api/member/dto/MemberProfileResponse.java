package matchuri.backend.api.member.dto;

public record MemberProfileResponse(
        Long id,
        String loginId,
        String email,
        MemberTasteProfileSummaryResponse memberTasteProfile
) {
}
