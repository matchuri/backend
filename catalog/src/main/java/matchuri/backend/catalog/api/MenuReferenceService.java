package matchuri.backend.catalog.api;

import java.util.List;
import matchuri.backend.catalog.command.GetAttributeCategoriesCommand;
import matchuri.backend.catalog.command.GetRestrictionIngredientsCommand;
import matchuri.backend.catalog.command.SearchMenuItemsCommand;
import matchuri.backend.catalog.result.AttributeCategoryResult;
import matchuri.backend.catalog.result.MenuItemDetailResult;
import matchuri.backend.catalog.result.MenuItemSummaryResult;
import matchuri.backend.catalog.result.RestrictionIngredientResult;

public interface MenuReferenceService {

    List<AttributeCategoryResult> getActiveAttributeCategories(GetAttributeCategoriesCommand command);

    List<RestrictionIngredientResult> getActiveRestrictionIngredients(GetRestrictionIngredientsCommand command);

    List<MenuItemSummaryResult> searchMenuItems(SearchMenuItemsCommand command);

    MenuItemDetailResult getMenuItem(Long menuItemId);
}
