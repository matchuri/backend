package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteLink;

public record GroupInviteLinkResult(
        Long groupId,
        String token,
        LocalDateTime expiresAt
) {
    public static GroupInviteLinkResult from(GroupInviteLink inviteLink) {
        return new GroupInviteLinkResult(
                inviteLink.getRoom().getId(),
                inviteLink.getToken(),
                inviteLink.getExpiresAt()
        );
    }
}
