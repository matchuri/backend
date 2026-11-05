package matchuri.backend.domain.member.result;

public record MemberPresetProfileImageResult(
        Long presetProfileImageId,
        String imageUrl,
        boolean isDefault
) {
}