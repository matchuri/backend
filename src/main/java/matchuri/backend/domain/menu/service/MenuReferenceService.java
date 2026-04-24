package matchuri.backend.domain.menu.service;

import java.util.List;
import matchuri.backend.domain.menu.command.SearchMenuItemsCommand;
import matchuri.backend.domain.menu.result.AttributeCategoryResult;
import matchuri.backend.domain.menu.result.MenuItemDetailResult;
import matchuri.backend.domain.menu.result.MenuItemSummaryResult;
import matchuri.backend.domain.menu.result.RestrictionIngredientResult;

public interface MenuReferenceService {

    List<AttributeCategoryResult> getActiveAttributeCategories();

    List<RestrictionIngredientResult> getActiveRestrictionIngredients();

    List<MenuItemSummaryResult> searchMenuItems(SearchMenuItemsCommand command);

    MenuItemDetailResult getMenuItem(Long menuItemId);
}
