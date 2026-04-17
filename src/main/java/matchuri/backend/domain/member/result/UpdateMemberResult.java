package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;

public record UpdateMemberResult(
        Long id,
        LocalDateTime updatedAt
) {
}
