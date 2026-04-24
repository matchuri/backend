package matchuri.backend.domain.menu.service;

import java.util.List;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.CreateAdminIngredientCommand;
import matchuri.backend.domain.menu.command.UpdateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.UpdateAdminIngredientCommand;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;
import matchuri.backend.domain.menu.result.AdminIngredientResult;

public interface MenuAdminReferenceService {

    List<AdminAttributeCategoryResult> getAttributeCategories();

    List<AdminIngredientResult> getIngredients();

    AdminAttributeCategoryResult createAttributeCategory(CreateAdminAttributeCategoryCommand command);

    AdminIngredientResult createIngredient(CreateAdminIngredientCommand command);

    AdminAttributeCategoryResult updateAttributeCategory(UpdateAdminAttributeCategoryCommand command);

    AdminIngredientResult updateIngredient(UpdateAdminIngredientCommand command);

    AdminAttributeCategoryResult deactivateAttributeCategory(Long attributeCategoryId);

    AdminIngredientResult deactivateIngredient(Long ingredientId);
}
