package matchuri.backend.api.menu;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.menu.dto.response.AttributeCategoryResponse;
import matchuri.backend.api.menu.dto.response.RestrictionIngredientResponse;
import matchuri.backend.api.menu.mapper.MenuReferenceMapper;
import matchuri.backend.domain.menu.service.MenuReferenceService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuReferenceController implements MenuReferenceApi {

    private final MenuReferenceService menuReferenceService;
    private final MenuReferenceMapper menuReferenceMapper;

    @Override
    @GetMapping("/attribute-categories")
    public ApiResponse<List<AttributeCategoryResponse>> getAttributeCategories() {
        return ApiResponse.success(
                menuReferenceMapper.toAttributeCategoryResponses(menuReferenceService.getActiveAttributeCategories())
        );
    }

    @Override
    @GetMapping("/restriction-ingredients")
    public ApiResponse<List<RestrictionIngredientResponse>> getRestrictionIngredients() {
        return ApiResponse.success(
                menuReferenceMapper.toRestrictionIngredientResponses(
                        menuReferenceService.getActiveRestrictionIngredients())
        );
    }
}
