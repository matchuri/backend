package matchuri.backend.domain.member.service;

import java.time.LocalDateTime;

public record UpdateMemberResult(
        Long id,
        LocalDateTime updatedAt
) {
}
