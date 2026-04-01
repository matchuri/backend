package matchuri.backend.api.member.dto;

import java.time.LocalDateTime;

public record CreateMemberResponse(
        Long memberId,
        String loginId,
        LocalDateTime createdAt
) {
}
