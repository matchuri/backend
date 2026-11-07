package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.member.entity.Member;

public record RegisterLocalMemberResult(
        Long memberId,
        String loginId,
        String email,
        String nickname,
        LocalDateTime createdAt
) {

    public static RegisterLocalMemberResult from(Member member) {
        return new RegisterLocalMemberResult(
                member.getId(),
                member.getLoginId(),
                member.getEmail(),
                member.getNickname(),
                member.getCreatedAt()
        );
    }
}
