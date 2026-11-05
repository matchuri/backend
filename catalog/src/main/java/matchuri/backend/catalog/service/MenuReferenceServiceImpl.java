package matchuri.backend.catalog.service;

import matchuri.backend.catalog.api.MenuReferenceService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.media.support.ImageUrlResolver;
import matchuri.backend.catalog.MenuErrorCode;
import matchuri.backend.catalog.command.GetAttributeCategoriesCommand;
import matchuri.backend.catalog.command.GetRestrictionIngredientsCommand;
import matchuri.backend.catalog.command.SearchMenuItemsCommand;
import matchuri.backend.catalog.entity.CategoryType;
import matchuri.backend.catalog.repository.AttributeCategoryRepository;
import matchuri.backend.catalog.repository.IngredientRepository;
import matchuri.backend.catalog.repository.MenuAttributeCategoryRepository;
import matchuri.backend.catalog.repository.MenuIngredientRepository;
import matchuri.backend.catalog.repository.MenuItemDetailRow;
import matchuri.backend.catalog.repository.MenuItemRepository;
import matchuri.backend.catalog.result.AttributeCategoryResult;
import matchuri.backend.catalog.result.MenuItemDetailResult;
import matchuri.backend.catalog.result.MenuItemSummaryResult;
import matchuri.backend.catalog.result.RestrictionIngredientResult;
import matchuri.backend.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuReferenceServiceImpl implements MenuReferenceService {

    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuAttributeCategoryRepository menuAttributeCategoryRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final MenuItemRepository menuItemRepository;
    private final ImageUrlResolver imageUrlResolver;

    @Override
    public List<AttributeCategoryResult> getActiveAttributeCategories(GetAttributeCategoriesCommand command) {
        List<CategoryType> categoryTypes = command.categoryTypes() == null ? List.of()
                : command.categoryTypes().stream().distinct().toList();

        var categories = categoryTypes.isEmpty()
                ? attributeCategoryRepository.findAllByActiveTrueOrderByCategoryTypeAscSortOrderAscIdAsc()
                : attributeCategoryRepository
                        .findAllByActiveTrueAndCategoryTypeInOrderByCategoryTypeAscSortOrderAscIdAsc(categoryTypes);

        return categories.stream()
                .map(AttributeCategoryResult::from)
                .toList();
    }

    @Override
    public List<RestrictionIngredientResult> getActiveRestrictionIngredients(GetRestrictionIngredientsCommand command) {
        return ingredientRepository.searchActiveRestrictionIngredients(
                        normalizeQuery(command.query()),
                        command.allergen()
                )
                .stream()
                .map(RestrictionIngredientResult::from)
                .toList();
    }

    @Override
    public List<MenuItemSummaryResult> searchMenuItems(SearchMenuItemsCommand command) {
        List<Long> attributeCategoryIds = distinctIds(command.attributeCategoryIds());
        List<Long> ingredientIds = distinctIds(command.ingredientIds());

        validateActiveAttributeCategoryIds(attributeCategoryIds);
        validateActiveIngredientIds(ingredientIds);

        List<Long> attributeCategoryIdsForQuery = idsForQuery(attributeCategoryIds);
        List<Long> ingredientIdsForQuery = idsForQuery(ingredientIds);

        return menuItemRepository.searchActiveMenuItems(
                        normalizeQuery(command.query()),
                        attributeCategoryIdsForQuery,
                        attributeCategoryIds.isEmpty(),
                        ingredientIdsForQuery,
                        ingredientIds.isEmpty()
                )
                .stream()
                .map(MenuItemSummaryResult::from)
                .toList();
    }

    @Override
    public MenuItemDetailResult getMenuItem(Long menuItemId) {
        MenuItemDetailRow menuItem = menuItemRepository.findActiveDetailRowById(menuItemId)
                .orElseThrow(() -> new BusinessException(MenuErrorCode.NOT_FOUND, menuItemId));

        return new MenuItemDetailResult(
                menuItem.id(),
                menuItem.code(),
                menuItem.name(),
                menuItem.description(),
                imageUrlResolver.toPublicUrl(menuItem.thumbnailObjectKey()),
                menuAttributeCategoryRepository.findActiveRowsByMenuId(menuItemId).stream()
                        .map(category -> new AttributeCategoryResult(
                                category.id(),
                                category.categoryType(),
                                category.code(),
                                category.name(),
                                category.sortOrder()
                        ))
                        .toList(),
                menuIngredientRepository.findActiveRowsByMenuId(menuItemId).stream()
                        .map(ingredient -> new RestrictionIngredientResult(
                                ingredient.id(),
                                ingredient.code(),
                                ingredient.name(),
                                ingredient.allergen(),
                                ingredient.sortOrder()
                        ))
                        .toList()
        );
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }

        return ids.stream()
                .distinct()
                .toList();
    }

    private void validateActiveAttributeCategoryIds(List<Long> attributeCategoryIds) {
        if (attributeCategoryIds.isEmpty()) {
            return;
        }

        int activeCount = attributeCategoryRepository.findAllByIdInAndActiveTrue(attributeCategoryIds).size();
        if (activeCount != attributeCategoryIds.size()) {
            throw new BusinessException(MenuErrorCode.INVALID_FILTER, "attributeCategoryIds", attributeCategoryIds);
        }
    }

    private void validateActiveIngredientIds(List<Long> ingredientIds) {
        if (ingredientIds.isEmpty()) {
            return;
        }

        int activeCount = ingredientRepository.findAllByIdInAndActiveTrue(ingredientIds).size();
        if (activeCount != ingredientIds.size()) {
            throw new BusinessException(MenuErrorCode.INVALID_FILTER, "ingredientIds", ingredientIds);
        }
    }

    private List<Long> idsForQuery(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of(-1L);
        }

        return ids;
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return query.trim();
    }
}
