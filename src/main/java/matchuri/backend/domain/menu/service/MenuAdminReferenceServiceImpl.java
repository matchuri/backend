package matchuri.backend.domain.menu.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.MenuErrorCode;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.CreateAdminIngredientCommand;
import matchuri.backend.domain.menu.command.UpdateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.UpdateAdminIngredientCommand;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;
import matchuri.backend.domain.menu.result.AdminIngredientResult;
import matchuri.backend.domain.menu.result.AdminMenuItemResult;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuAdminReferenceServiceImpl implements MenuAdminReferenceService {

    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public List<AdminAttributeCategoryResult> getAttributeCategories() {
        return attributeCategoryRepository.findAllByOrderByCategoryTypeAscSortOrderAscIdAsc().stream()
                .map(AdminAttributeCategoryResult::from)
                .toList();
    }

    @Override
    public List<AdminIngredientResult> getIngredients() {
        return ingredientRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(AdminIngredientResult::from)
                .toList();
    }

    @Override
    public List<AdminMenuItemResult> getMenuItems() {
        return menuItemRepository.findAllByOrderByIdAsc().stream()
                .map(AdminMenuItemResult::from)
                .toList();
    }

    @Override
    @Transactional
    public AdminAttributeCategoryResult createAttributeCategory(CreateAdminAttributeCategoryCommand command) {
        if (attributeCategoryRepository.existsByCategoryTypeAndCode(command.categoryType(), command.code())) {
            throw new BusinessException(MenuErrorCode.ATTRIBUTE_CATEGORY_DUPLICATE, command.categoryType(),
                    command.code());
        }

        AttributeCategory attributeCategory = attributeCategoryRepository.saveAndFlush(
                new AttributeCategory(command.categoryType(), command.code(), command.name(), command.sortOrder())
        );

        return AdminAttributeCategoryResult.from(attributeCategory);
    }

    @Override
    @Transactional
    public AdminIngredientResult createIngredient(CreateAdminIngredientCommand command) {
        if (ingredientRepository.existsByCode(command.code())) {
            throw new BusinessException(MenuErrorCode.INGREDIENT_DUPLICATE, command.code());
        }

        Ingredient ingredient = ingredientRepository.saveAndFlush(
                new Ingredient(command.code(), command.name(), command.allergen(), command.sortOrder())
        );

        return AdminIngredientResult.from(ingredient);
    }

    @Override
    @Transactional
    public AdminAttributeCategoryResult updateAttributeCategory(UpdateAdminAttributeCategoryCommand command) {
        AttributeCategory attributeCategory = attributeCategoryRepository.findById(command.attributeCategoryId())
                .orElseThrow(() -> new BusinessException(
                        MenuErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND,
                        command.attributeCategoryId()
                ));

        if (command.name() != null) {
            attributeCategory.updateName(command.name());
        }

        if (command.sortOrder() != null) {
            attributeCategory.updateSortOrder(command.sortOrder());
        }

        if (command.isActive() != null) {
            if (command.isActive()) {
                attributeCategory.activate();
            } else {
                attributeCategory.deactivate();
            }
        }

        return AdminAttributeCategoryResult.from(attributeCategory);
    }

    @Override
    @Transactional
    public AdminIngredientResult updateIngredient(UpdateAdminIngredientCommand command) {
        Ingredient ingredient = ingredientRepository.findById(command.ingredientId())
                .orElseThrow(() -> new BusinessException(
                        MenuErrorCode.INGREDIENT_NOT_FOUND,
                        command.ingredientId()
                ));

        if (command.name() != null) {
            ingredient.updateName(command.name());
        }

        if (command.allergen() != null) {
            ingredient.updateAllergen(command.allergen());
        }

        if (command.sortOrder() != null) {
            ingredient.updateSortOrder(command.sortOrder());
        }

        if (command.isActive() != null) {
            if (command.isActive()) {
                ingredient.activate();
            } else {
                ingredient.deactivate();
            }
        }

        return AdminIngredientResult.from(ingredient);
    }

    @Override
    @Transactional
    public AdminAttributeCategoryResult deactivateAttributeCategory(Long attributeCategoryId) {
        AttributeCategory attributeCategory = attributeCategoryRepository.findById(attributeCategoryId)
                .orElseThrow(() -> new BusinessException(
                        MenuErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND,
                        attributeCategoryId
                ));

        if (attributeCategory.isActive()) {
            attributeCategory.deactivate();
        }

        return AdminAttributeCategoryResult.from(attributeCategory);
    }

    @Override
    @Transactional
    public AdminIngredientResult deactivateIngredient(Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new BusinessException(MenuErrorCode.INGREDIENT_NOT_FOUND, ingredientId));

        if (ingredient.isActive()) {
            ingredient.deactivate();
        }

        return AdminIngredientResult.from(ingredient);
    }
}
