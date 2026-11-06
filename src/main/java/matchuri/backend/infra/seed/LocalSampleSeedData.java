package matchuri.backend.infra.seed;

import java.math.BigDecimal;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.menu.entity.CategoryType;

public record LocalSampleSeedData(
        List<MemberSeed> members,
        List<AgreementSeed> agreements,
        List<TasteProfileSeed> tasteProfiles,
        List<GroupSeed> groups
) {

    public record MemberSeed(
            String loginId,
            String passwordHash,
            String nickname,
            String email,
            MemberRole role
    ) {
    }

    public record AgreementSeed(
            AgreementType type,
            String version
    ) {
    }

    public record TasteProfileSeed(
            String loginId,
            String profileVersion,
            List<AttributeCategoryRef> preferredAttributeCategories,
            List<String> restrictionIngredientCodes,
            List<String> dislikedMenuCodes
    ) {
    }

    public record AttributeCategoryRef(
            CategoryType categoryType,
            String code
    ) {
    }

    public record GroupSeed(
            String name,
            String inviteCode,
            String hostLoginId,
            LocationSeed location,
            List<GroupMemberSeed> members
    ) {
    }

    public record LocationSeed(
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            String address
    ) {
    }

    public record GroupMemberSeed(
            String loginId,
            GroupMemberRole role
    ) {
    }
}
