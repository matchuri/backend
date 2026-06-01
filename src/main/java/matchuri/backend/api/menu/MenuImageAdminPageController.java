package matchuri.backend.api.menu;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.service.MenuImageAdminService;
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

    @GetMapping("/{menuItemId}")
    public String getMenuItemDetailPage(@PathVariable Long menuItemId, Model model) {
        model.addAttribute("menuItem", menuImageAdminService.getAdminMenuItemDetail(menuItemId));

        return "admin/menu-item-detail";
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
}
