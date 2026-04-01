package matchuri.backend.api.member.dto;

import java.time.LocalDateTime;

public record UpdateMemberResponse(
        Long id,
        LocalDateTime updatedAt
) {
}
