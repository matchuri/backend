package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;

public interface MemberTasteProfileRestrictionIngredientRepositoryCustom {

    List<MemberTasteProfileRestrictionIngredient> findAllByProfileIdOrderByDisplay(Long profileId);
}
