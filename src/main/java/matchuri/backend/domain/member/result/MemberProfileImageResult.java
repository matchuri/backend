package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;

public record MemberProfileImageResult(
        Long profileImageId,
        Long presetProfileImageId,
        String imageUrl,
        LocalDateTime updatedAt
) {
}
