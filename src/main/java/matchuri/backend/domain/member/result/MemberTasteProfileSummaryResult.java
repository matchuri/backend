package matchuri.backend.domain.member.result;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import matchuri.backend.domain.menu.entity.CategoryType;

public record MemberTasteProfileSummaryResult(
        Long memberId,
        String profileVersion,
        List<AttributeCategoryItem> attributeCategories,
        List<RestrictionIngredientItem> restrictionIngredients,
        LocalDateTime updatedAt
) {

    public static final String DEFAULT_PROFILE_VERSION = "v1";

    public static MemberTasteProfileSummaryResult empty(Long memberId) {
        return new MemberTasteProfileSummaryResult(
                memberId,
                DEFAULT_PROFILE_VERSION,
                List.of(),
                List.of(),
                null
        );
    }

    public static MemberTasteProfileSummaryResult of(
            Long memberId,
            MemberTasteProfile profile,
            List<MemberTasteProfileCategory> attributeCategories,
            List<MemberTasteProfileRestrictionIngredient> restrictionIngredients
    ) {
        return new MemberTasteProfileSummaryResult(
                memberId,
                profile.getProfileVersion(),
                attributeCategories.stream()
                        .map(AttributeCategoryItem::from)
                        .toList(),
                restrictionIngredients.stream()
                        .map(RestrictionIngredientItem::from)
                        .toList(),
                profile.getUpdatedAt()
        );
    }

    public record AttributeCategoryItem(
            Long id,
            CategoryType categoryType,
            String code,
            String name,
            int sortOrder
    ) {

        public static AttributeCategoryItem from(MemberTasteProfileCategory mapping) {
            var attributeCategory = mapping.getAttributeCategory();
            return new AttributeCategoryItem(
                    attributeCategory.getId(),
                    attributeCategory.getCategoryType(),
                    attributeCategory.getCode(),
                    attributeCategory.getName(),
                    attributeCategory.getSortOrder()
            );
        }
    }

    public record RestrictionIngredientItem(
            Long id,
            String code,
            String name,
            boolean allergen,
            int sortOrder
    ) {

        public static RestrictionIngredientItem from(MemberTasteProfileRestrictionIngredient mapping) {
            var ingredient = mapping.getIngredient();
            return new RestrictionIngredientItem(
                    ingredient.getId(),
                    ingredient.getCode(),
                    ingredient.getName(),
                    ingredient.isAllergen(),
                    ingredient.getSortOrder()
            );
        }
    }
}
