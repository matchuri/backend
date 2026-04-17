package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;

public record RegisterLocalMemberResult(
        Long memberId,
        String loginId,
        String nickname,
        LocalDateTime createdAt
) {
}
