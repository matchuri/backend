package matchuri.backend.domain.member.service;

import java.time.LocalDateTime;

public record CreateMemberResult(
        Long memberId,
        String loginId,
        LocalDateTime createdAt
) {
}
