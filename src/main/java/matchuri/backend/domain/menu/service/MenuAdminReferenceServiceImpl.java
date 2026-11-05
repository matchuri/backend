package matchuri.backend.domain.menu.service;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.MenuErrorCode;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.CreateAdminIngredientCommand;
import matchuri.backend.domain.menu.command.CreateAdminMenuItemCommand;
import matchuri.backend.domain.menu.command.UpdateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.UpdateAdminIngredientCommand;
import matchuri.backend.domain.menu.command.UpdateAdminMenuItemCommand;
import matchuri.backend.domain.menu.command.UpdateAdminMenuItemReferencesCommand;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;
import matchuri.backend.domain.menu.result.AdminIngredientResult;
import matchuri.backend.domain.menu.result.AdminMenuItemDetailResult;
import matchuri.backend.domain.menu.result.AdminMenuItemResult;
import matchuri.backend.domain.menu.support.MenuThumbnailUrlResolver;
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
    private final MenuAttributeCategoryRepository menuAttributeCategoryRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final MenuThumbnailUrlResolver menuThumbnailUrlResolver;

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
    public AdminMenuItemDetailResult getMenuItemDetail(Long menuItemId) {
        MenuItem menuItem = getMenuItem(menuItemId);

        return toAdminMenuItemDetailResult(menuItem);
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
    public AdminMenuItemDetailResult createMenuItem(CreateAdminMenuItemCommand command) {
        validateNoDuplicateIds(command.attributeCategoryIds(), MenuErrorCode.DUPLICATE_MENU_ATTRIBUTE_CATEGORY);
        validateNoDuplicateIds(command.ingredientIds(), MenuErrorCode.DUPLICATE_MENU_INGREDIENT);

        if (menuItemRepository.existsByCode(command.code())) {
            throw new BusinessException(MenuErrorCode.DUPLICATE, command.code());
        }

        List<AttributeCategory> attributeCategories = loadActiveAttributeCategories(command.attributeCategoryIds());
        List<Ingredient> ingredients = loadActiveIngredients(command.ingredientIds());

        MenuItem menuItem = menuItemRepository.saveAndFlush(
                new MenuItem(command.code(), command.name(), command.description())
        );

        saveMenuAttributeCategories(menuItem, attributeCategories);
        saveMenuIngredients(menuItem, ingredients);

        return toAdminMenuItemDetailResult(menuItem);
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
    public AdminMenuItemResult updateMenuItem(UpdateAdminMenuItemCommand command) {
        MenuItem menuItem = getMenuItem(command.menuItemId());

        if (command.name() != null) {
            menuItem.updateName(command.name());
        }

        if (command.description() != null) {
            menuItem.updateDescription(command.description());
        }

        if (command.isActive() != null) {
            if (command.isActive()) {
                menuItem.activate();
            } else {
                menuItem.deactivate();
            }
        }

        return AdminMenuItemResult.from(menuItem);
    }

    @Override
    @Transactional
    public AdminMenuItemDetailResult updateMenuItemReferences(UpdateAdminMenuItemReferencesCommand command) {
        validateNoDuplicateIds(command.attributeCategoryIds(), MenuErrorCode.DUPLICATE_MENU_ATTRIBUTE_CATEGORY);
        validateNoDuplicateIds(command.ingredientIds(), MenuErrorCode.DUPLICATE_MENU_INGREDIENT);

        MenuItem menuItem = getMenuItem(command.menuItemId());
        List<AttributeCategory> attributeCategories = loadActiveAttributeCategories(command.attributeCategoryIds());
        List<Ingredient> ingredients = loadActiveIngredients(command.ingredientIds());

        replaceMenuAttributeCategories(menuItem, attributeCategories);
        replaceMenuIngredients(menuItem, ingredients);

        return toAdminMenuItemDetailResult(menuItem);
    }

    @Override
    @Transactional
    public AdminMenuItemResult deactivateMenuItem(Long menuItemId) {
        MenuItem menuItem = getMenuItem(menuItemId);

        if (menuItem.isActive()) {
            menuItem.deactivate();
        }

        return AdminMenuItemResult.from(menuItem);
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

    private MenuItem getMenuItem(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new BusinessException(MenuErrorCode.NOT_FOUND, menuItemId));
    }

    private AdminMenuItemDetailResult toAdminMenuItemDetailResult(MenuItem menuItem) {
        List<AdminAttributeCategoryResult> attributeCategories = menuAttributeCategoryRepository
                .findAllByMenuIdOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
                        menuItem.getId())
                .stream()
                .map(MenuAttributeCategory::getAttributeCategory)
                .map(AdminAttributeCategoryResult::from)
                .toList();

        List<AdminIngredientResult> ingredients = menuIngredientRepository
                .findAllByMenuIdOrderByIngredientSortOrderAscIngredientIdAsc(menuItem.getId())
                .stream()
                .map(MenuIngredient::getIngredient)
                .map(AdminIngredientResult::from)
                .toList();

        return new AdminMenuItemDetailResult(
                menuItem.getId(),
                menuItem.getCode(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.isActive(),
                menuThumbnailUrlResolver.resolve(menuItem.getId()),
                attributeCategories,
                ingredients
        );
    }

    private List<AttributeCategory> loadActiveAttributeCategories(List<Long> attributeCategoryIds) {
        if (attributeCategoryIds.isEmpty()) {
            return List.of();
        }

        List<AttributeCategory> attributeCategories = attributeCategoryRepository.findAllByIdInAndActiveTrue(
                attributeCategoryIds);
        if (attributeCategories.size() != attributeCategoryIds.size()) {
            throw new BusinessException(MenuErrorCode.INVALID_MENU_ATTRIBUTE_CATEGORY, attributeCategoryIds);
        }

        return sortByRequestedIdOrder(attributeCategories, attributeCategoryIds, AttributeCategory::getId);
    }

    private List<Ingredient> loadActiveIngredients(List<Long> ingredientIds) {
        if (ingredientIds.isEmpty()) {
            return List.of();
        }

        List<Ingredient> ingredients = ingredientRepository.findAllByIdInAndActiveTrue(ingredientIds);
        if (ingredients.size() != ingredientIds.size()) {
            throw new BusinessException(MenuErrorCode.INVALID_MENU_INGREDIENT, ingredientIds);
        }

        return sortByRequestedIdOrder(ingredients, ingredientIds, Ingredient::getId);
    }

    private <T> List<T> sortByRequestedIdOrder(List<T> values, List<Long> ids, Function<T, Long> idGetter) {
        var valuesById = values.stream()
                .collect(Collectors.toMap(idGetter, Function.identity()));

        return ids.stream()
                .map(valuesById::get)
                .toList();
    }

    private void validateNoDuplicateIds(List<Long> ids, MenuErrorCode errorCode) {
        Set<Long> distinctIds = ids.stream().collect(Collectors.toSet());
        if (distinctIds.size() != ids.size()) {
            throw new BusinessException(errorCode, ids);
        }
    }

    private void replaceMenuAttributeCategories(MenuItem menuItem, List<AttributeCategory> attributeCategories) {
        List<MenuAttributeCategory> currentMappings = menuAttributeCategoryRepository.findAllByMenuIdOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
                menuItem.getId());
        menuAttributeCategoryRepository.deleteAll(currentMappings);
        menuAttributeCategoryRepository.flush();

        saveMenuAttributeCategories(menuItem, attributeCategories);
    }

    private void replaceMenuIngredients(MenuItem menuItem, List<Ingredient> ingredients) {
        List<MenuIngredient> currentMappings = menuIngredientRepository.findAllByMenuIdOrderByIngredientSortOrderAscIngredientIdAsc(
                menuItem.getId());
        menuIngredientRepository.deleteAll(currentMappings);
        menuIngredientRepository.flush();

        saveMenuIngredients(menuItem, ingredients);
    }

    private void saveMenuAttributeCategories(MenuItem menuItem, List<AttributeCategory> attributeCategories) {
        menuAttributeCategoryRepository.saveAll(attributeCategories.stream()
                .map(attributeCategory -> new MenuAttributeCategory(menuItem, attributeCategory))
                .toList());
    }

    private void saveMenuIngredients(MenuItem menuItem, List<Ingredient> ingredients) {
        menuIngredientRepository.saveAll(ingredients.stream()
                .map(ingredient -> new MenuIngredient(menuItem, ingredient))
                .toList());
    }
}
