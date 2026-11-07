package matchuri.backend.domain.menu.service;

import java.util.List;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.CreateAdminIngredientCommand;
import matchuri.backend.domain.menu.command.CreateAdminMenuItemCommand;
import matchuri.backend.domain.menu.command.UpdateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.UpdateAdminIngredientCommand;
import matchuri.backend.domain.menu.command.UpdateAdminMenuItemCommand;
import matchuri.backend.domain.menu.command.UpdateAdminMenuItemReferencesCommand;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;
import matchuri.backend.domain.menu.result.AdminIngredientResult;
import matchuri.backend.domain.menu.result.AdminMenuItemDetailResult;
import matchuri.backend.domain.menu.result.AdminMenuItemResult;

public interface MenuAdminReferenceService {

    List<AdminAttributeCategoryResult> getAttributeCategories();

    List<AdminIngredientResult> getIngredients();

    List<AdminMenuItemResult> getMenuItems();

    AdminMenuItemDetailResult getMenuItemDetail(Long menuItemId);

    AdminAttributeCategoryResult createAttributeCategory(CreateAdminAttributeCategoryCommand command);

    AdminIngredientResult createIngredient(CreateAdminIngredientCommand command);

    AdminMenuItemDetailResult createMenuItem(CreateAdminMenuItemCommand command);

    AdminAttributeCategoryResult updateAttributeCategory(UpdateAdminAttributeCategoryCommand command);

    AdminIngredientResult updateIngredient(UpdateAdminIngredientCommand command);

    AdminMenuItemResult updateMenuItem(UpdateAdminMenuItemCommand command);

    AdminMenuItemDetailResult updateMenuItemReferences(UpdateAdminMenuItemReferencesCommand command);

    AdminMenuItemResult deactivateMenuItem(Long menuItemId);

    AdminAttributeCategoryResult deactivateAttributeCategory(Long attributeCategoryId);

    AdminIngredientResult deactivateIngredient(Long ingredientId);
}
