package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;

public interface MemberTasteProfileCategoryRepositoryCustom {

    List<MemberTasteProfileCategory> findAllByProfileIdOrderByDisplay(Long profileId);

    List<MemberTasteProfileAttributeCategoryRow> findAttributeCategoryRowsByProfileId(Long profileId);
}
