package matchuri.backend.api.menu;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.menu.dto.request.CreateAdminAttributeCategoryRequest;
import matchuri.backend.api.menu.dto.request.CreateAdminIngredientRequest;
import matchuri.backend.api.menu.dto.request.UpdateAdminAttributeCategoryRequest;
import matchuri.backend.api.menu.dto.request.UpdateAdminIngredientRequest;
import matchuri.backend.api.menu.mapper.MenuReferenceMapper;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.service.MenuAdminReferenceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminReferencePageController {

    private final MenuAdminReferenceService menuAdminReferenceService;
    private final MenuReferenceMapper menuReferenceMapper;

    @GetMapping("/attribute-categories")
    public String getAttributeCategoriesPage(Model model) {
        model.addAttribute("categoryTypes", CategoryType.values());
        model.addAttribute("attributeCategories", menuAdminReferenceService.getAttributeCategories());

        return "admin/attribute-categories";
    }

    @PostMapping("/attribute-categories")
    public String createAttributeCategory(
            @RequestParam String categoryType,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam Integer sortOrder,
            RedirectAttributes redirectAttributes
    ) {
        var command = menuReferenceMapper.toCreateAdminAttributeCategoryCommand(
                new CreateAdminAttributeCategoryRequest(categoryType, code, name, sortOrder)
        );
        menuAdminReferenceService.createAttributeCategory(command);
        redirectAttributes.addFlashAttribute("message", "속성 카테고리가 생성되었습니다.");

        return "redirect:/admin/attribute-categories";
    }

    @PostMapping("/attribute-categories/{attributeCategoryId}")
    public String updateAttributeCategory(
            @PathVariable Long attributeCategoryId,
            @RequestParam String name,
            @RequestParam Integer sortOrder,
            @RequestParam Boolean isActive,
            RedirectAttributes redirectAttributes
    ) {
        var command = menuReferenceMapper.toUpdateAdminAttributeCategoryCommand(
                attributeCategoryId,
                new UpdateAdminAttributeCategoryRequest(name, sortOrder, isActive)
        );
        menuAdminReferenceService.updateAttributeCategory(command);
        redirectAttributes.addFlashAttribute("message", "속성 카테고리가 저장되었습니다.");

        return "redirect:/admin/attribute-categories";
    }

    @PostMapping("/attribute-categories/{attributeCategoryId}/deactivate")
    public String deactivateAttributeCategory(
            @PathVariable Long attributeCategoryId,
            RedirectAttributes redirectAttributes
    ) {
        menuAdminReferenceService.deactivateAttributeCategory(attributeCategoryId);
        redirectAttributes.addFlashAttribute("message", "속성 카테고리가 비활성화되었습니다.");

        return "redirect:/admin/attribute-categories";
    }

    @GetMapping("/ingredients")
    public String getIngredientsPage(Model model) {
        model.addAttribute("ingredients", menuAdminReferenceService.getIngredients());

        return "admin/ingredients";
    }

    @PostMapping("/ingredients")
    public String createIngredient(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(defaultValue = "false") Boolean allergen,
            @RequestParam Integer sortOrder,
            RedirectAttributes redirectAttributes
    ) {
        var command = menuReferenceMapper.toCreateAdminIngredientCommand(
                new CreateAdminIngredientRequest(code, name, allergen, sortOrder)
        );
        menuAdminReferenceService.createIngredient(command);
        redirectAttributes.addFlashAttribute("message", "재료가 생성되었습니다.");

        return "redirect:/admin/ingredients";
    }

    @PostMapping("/ingredients/{ingredientId}")
    public String updateIngredient(
            @PathVariable Long ingredientId,
            @RequestParam String name,
            @RequestParam(defaultValue = "false") Boolean allergen,
            @RequestParam Integer sortOrder,
            @RequestParam Boolean isActive,
            RedirectAttributes redirectAttributes
    ) {
        var command = menuReferenceMapper.toUpdateAdminIngredientCommand(
                ingredientId,
                new UpdateAdminIngredientRequest(name, allergen, sortOrder, isActive)
        );
        menuAdminReferenceService.updateIngredient(command);
        redirectAttributes.addFlashAttribute("message", "재료가 저장되었습니다.");

        return "redirect:/admin/ingredients";
    }

    @PostMapping("/ingredients/{ingredientId}/deactivate")
    public String deactivateIngredient(
            @PathVariable Long ingredientId,
            RedirectAttributes redirectAttributes
    ) {
        menuAdminReferenceService.deactivateIngredient(ingredientId);
        redirectAttributes.addFlashAttribute("message", "재료가 비활성화되었습니다.");

        return "redirect:/admin/ingredients";
    }
}
