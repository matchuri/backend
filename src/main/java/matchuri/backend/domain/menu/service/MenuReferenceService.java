package matchuri.backend.domain.menu.service;

import java.util.List;
import matchuri.backend.domain.menu.result.AttributeCategoryResult;
import matchuri.backend.domain.menu.result.RestrictionIngredientResult;

public interface MenuReferenceService {

    List<AttributeCategoryResult> getActiveAttributeCategories();

    List<RestrictionIngredientResult> getActiveRestrictionIngredients();
}
