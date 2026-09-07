package matchuri.backend.domain.member.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.menu.entity.CategoryType;

public record MemberHomeQueryResult(
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
        String profileVersion,
        LocalDateTime profileUpdatedAt,
        List<AttributeCategoryRow> attributeCategories
) {

    public record AttributeCategoryRow(
            Long id,
            CategoryType categoryType,
            String code,
            String name,
            Integer sortOrder
    ) {
    }
}
