package matchuri.backend.api.menu;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.menu.dto.request.CreateAdminMenuItemRequest;
import matchuri.backend.api.menu.dto.request.UpdateAdminMenuItemReferencesRequest;
import matchuri.backend.api.menu.dto.request.UpdateAdminMenuItemRequest;
import matchuri.backend.api.menu.mapper.MenuReferenceMapper;
import matchuri.backend.domain.menu.service.MenuImageAdminService;
import matchuri.backend.domain.menu.service.MenuAdminReferenceService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/menu-items")
public class MenuImageAdminPageController {

    private final MenuImageAdminService menuImageAdminService;
    private final MenuAdminReferenceService menuAdminReferenceService;
    private final MenuReferenceMapper menuReferenceMapper;

    @GetMapping
    public String getMenuItemsPage(Model model) {
        model.addAttribute("menuItems", menuAdminReferenceService.getMenuItems());

        return "admin/menu-items";
    }

    @GetMapping("/new")
    public String getNewMenuItemPage(Model model) {
        addReferenceData(model);

        return "admin/menu-item-form";
    }

    @PostMapping
    public String createMenuItem(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<Long> attributeCategoryIds,
            @RequestParam(required = false) List<Long> ingredientIds,
            RedirectAttributes redirectAttributes
    ) {
        var request = new CreateAdminMenuItemRequest(
                code,
                name,
                description,
                emptyIfNull(attributeCategoryIds),
                emptyIfNull(ingredientIds)
        );
        var command = menuReferenceMapper.toCreateAdminMenuItemCommand(request);
        var result = menuAdminReferenceService.createMenuItem(command);
        redirectAttributes.addFlashAttribute("message", "메뉴가 생성되었습니다.");

        return "redirect:/admin/menu-items/" + result.id();
    }

    @GetMapping("/{menuItemId}")
    public String getMenuItemDetailPage(@PathVariable Long menuItemId, Model model) {
        var menuItem = menuAdminReferenceService.getMenuItemDetail(menuItemId);
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("selectedAttributeCategoryIds", menuItem.attributeCategories().stream()
                .map(attributeCategory -> attributeCategory.id())
                .toList());
        model.addAttribute("selectedIngredientIds", menuItem.ingredients().stream()
                .map(ingredient -> ingredient.id())
                .toList());
        addReferenceData(model);

        return "admin/menu-item-detail";
    }

    @PostMapping("/{menuItemId}")
    public String updateMenuItem(
            @PathVariable Long menuItemId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Boolean isActive,
            RedirectAttributes redirectAttributes
    ) {
        var command = menuReferenceMapper.toUpdateAdminMenuItemCommand(
                menuItemId,
                new UpdateAdminMenuItemRequest(name, description, isActive)
        );
        menuAdminReferenceService.updateMenuItem(command);
        redirectAttributes.addFlashAttribute("message", "메뉴 정보가 저장되었습니다.");

        return "redirect:/admin/menu-items/" + menuItemId;
    }

    @PostMapping("/{menuItemId}/references")
    public String updateMenuItemReferences(
            @PathVariable Long menuItemId,
            @RequestParam(required = false) List<Long> attributeCategoryIds,
            @RequestParam(required = false) List<Long> ingredientIds,
            RedirectAttributes redirectAttributes
    ) {
        var request = new UpdateAdminMenuItemReferencesRequest(
                emptyIfNull(attributeCategoryIds),
                emptyIfNull(ingredientIds)
        );
        var command = menuReferenceMapper.toUpdateAdminMenuItemReferencesCommand(menuItemId, request);
        menuAdminReferenceService.updateMenuItemReferences(command);
        redirectAttributes.addFlashAttribute("message", "메뉴 연결 정보가 저장되었습니다.");

        return "redirect:/admin/menu-items/" + menuItemId;
    }

    @PostMapping(value = "/{menuItemId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadPrimaryMenuImage(
            @PathVariable Long menuItemId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        menuImageAdminService.uploadPrimaryImage(menuItemId, file);
        redirectAttributes.addFlashAttribute("message", "메뉴 이미지가 업로드되었습니다.");

        return "redirect:/admin/menu-items/" + menuItemId;
    }

    @PostMapping("/{menuItemId}/images/primary/delete")
    public String deletePrimaryMenuImage(
            @PathVariable Long menuItemId,
            RedirectAttributes redirectAttributes
    ) {
        menuImageAdminService.deletePrimaryImage(menuItemId);
        redirectAttributes.addFlashAttribute("message", "메뉴 이미지 연결이 제거되었습니다.");

        return "redirect:/admin/menu-items/" + menuItemId;
    }

    private void addReferenceData(Model model) {
        model.addAttribute("attributeCategories", menuAdminReferenceService.getAttributeCategories());
        model.addAttribute("ingredients", menuAdminReferenceService.getIngredients());
    }

    private List<Long> emptyIfNull(List<Long> ids) {
        return ids == null ? List.of() : ids;
    }
}
