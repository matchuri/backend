package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;

public record CreateMemberResult(
        Long memberId,
        String loginId,
        LocalDateTime createdAt
) {
}
