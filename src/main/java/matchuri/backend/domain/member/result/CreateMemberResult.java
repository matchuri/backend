package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.member.entity.Member;

public record CreateMemberResult(
        Long memberId,
        String loginId,
        LocalDateTime createdAt
) {

    public static CreateMemberResult from(Member member) {
        return new CreateMemberResult(
                member.getId(),
                member.getLoginId(),
                member.getCreatedAt()
        );
    }
}
