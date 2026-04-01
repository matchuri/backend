package matchuri.backend.global.security;

import matchuri.backend.domain.member.entity.MemberRole;

public record AuthenticatedMember(
        Long memberId,
        String loginId,
        MemberRole role
) {
}
