package matchuri.backend.domain.menu.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.MenuErrorCode;
import matchuri.backend.domain.menu.command.SearchMenuItemsCommand;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.menu.result.AttributeCategoryResult;
import matchuri.backend.domain.menu.result.MenuItemDetailResult;
import matchuri.backend.domain.menu.result.MenuItemSummaryResult;
import matchuri.backend.domain.menu.result.RestrictionIngredientResult;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuReferenceServiceImpl implements MenuReferenceService {

    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuAttributeCategoryRepository menuAttributeCategoryRepository;
    private final MenuIngredientRepository menuIngredientRepository;

    @Override
    public List<AttributeCategoryResult> getActiveAttributeCategories() {
        return attributeCategoryRepository.findAllByActiveTrueOrderByCategoryTypeAscSortOrderAscIdAsc().stream()
                .map(AttributeCategoryResult::from)
                .toList();
    }

    @Override
    public List<RestrictionIngredientResult> getActiveRestrictionIngredients() {
        return ingredientRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc().stream()
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
        var menuItem = menuItemRepository.findByIdAndActiveTrue(menuItemId)
                .orElseThrow(() -> new BusinessException(MenuErrorCode.NOT_FOUND, menuItemId));
        var attributeCategories = menuAttributeCategoryRepository
                .findAllByMenuIdAndAttributeCategoryActiveTrueOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
                        menuItemId)
                .stream()
                .map(menuAttributeCategory -> AttributeCategoryResult.from(menuAttributeCategory.getAttributeCategory()))
                .toList();
        var ingredients = menuIngredientRepository
                .findAllByMenuIdAndIngredientActiveTrueOrderByIngredientSortOrderAscIngredientIdAsc(menuItemId)
                .stream()
                .map(menuIngredient -> RestrictionIngredientResult.from(menuIngredient.getIngredient()))
                .toList();

        return MenuItemDetailResult.of(menuItem, attributeCategories, ingredients);
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
