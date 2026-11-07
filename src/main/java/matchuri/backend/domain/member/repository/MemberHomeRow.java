package matchuri.backend.domain.member.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberHomeRow(
        Long memberId,
        String loginId,
        String nickname,
        Boolean social,
        String email,
        String profileImageObjectKey,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        String address,
        Long tasteProfileId,
        String profileVersion,
        LocalDateTime profileUpdatedAt
) {
}
