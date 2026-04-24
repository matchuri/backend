package matchuri.backend.api.menu;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.menu.dto.response.AttributeCategoryResponse;
import matchuri.backend.api.menu.dto.response.MenuItemDetailResponse;
import matchuri.backend.api.menu.dto.response.MenuItemSummaryResponse;
import matchuri.backend.api.menu.dto.response.RestrictionIngredientResponse;
import matchuri.backend.api.menu.mapper.MenuReferenceMapper;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.service.MenuReferenceService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuReferenceController implements MenuReferenceApi {

    private final MenuReferenceService menuReferenceService;
    private final MenuReferenceMapper menuReferenceMapper;

    @Override
    @GetMapping("/attribute-categories")
    public ApiResponse<List<AttributeCategoryResponse>> getAttributeCategories(
            @RequestParam(required = false) List<CategoryType> categoryTypes
    ) {
        var command = menuReferenceMapper.toGetAttributeCategoriesCommand(categoryTypes);
        var categories = menuReferenceService.getActiveAttributeCategories(command);
        var responses = menuReferenceMapper.toAttributeCategoryResponses(categories);

        return ApiResponse.success(responses);
    }

    @Override
    @GetMapping("/restriction-ingredients")
    public ApiResponse<List<RestrictionIngredientResponse>> getRestrictionIngredients() {
        var ingredients = menuReferenceService.getActiveRestrictionIngredients();
        var responses = menuReferenceMapper.toRestrictionIngredientResponses(ingredients);

        return ApiResponse.success(responses);
    }

    @Override
    @GetMapping("/menu-items")
    public ApiResponse<List<MenuItemSummaryResponse>> searchMenuItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<Long> attributeCategoryIds,
            @RequestParam(required = false) List<Long> ingredientIds
    ) {
        var command = menuReferenceMapper.toSearchMenuItemsCommand(query, attributeCategoryIds, ingredientIds);
        var menuItems = menuReferenceService.searchMenuItems(command);
        var responses = menuReferenceMapper.toMenuItemSummaryResponses(menuItems);

        return ApiResponse.success(responses);
    }

    @Override
    @GetMapping("/menu-items/{menuItemId}")
    public ApiResponse<MenuItemDetailResponse> getMenuItem(@PathVariable Long menuItemId) {
        var menuItem = menuReferenceService.getMenuItem(menuItemId);
        var response = menuReferenceMapper.toMenuItemDetailResponse(menuItem);

        return ApiResponse.success(response);
    }
}
