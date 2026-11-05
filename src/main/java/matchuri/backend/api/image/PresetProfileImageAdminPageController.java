package matchuri.backend.api.image;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.image.service.PresetProfileImageAdminService;
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
@RequestMapping("/admin/preset-profile-images")
public class PresetProfileImageAdminPageController {

    private final PresetProfileImageAdminService presetProfileImageAdminService;

    @GetMapping
    public String getPresetProfileImagesPage(Model model) {
        model.addAttribute("presetProfileImages", presetProfileImageAdminService.getPresetProfileImages());
        return "admin/preset-profile-images";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        presetProfileImageAdminService.upload(file);
        redirectAttributes.addFlashAttribute("message", "프리셋 프로필 이미지가 추가되었습니다.");
        return "redirect:/admin/preset-profile-images";
    }

    @PostMapping("/{presetProfileImageId}/default")
    public String setDefault(
            @PathVariable Long presetProfileImageId,
            RedirectAttributes redirectAttributes
    ) {
        presetProfileImageAdminService.setDefault(presetProfileImageId);
        redirectAttributes.addFlashAttribute("message", "기본 프리셋 프로필 이미지가 변경되었습니다.");
        return "redirect:/admin/preset-profile-images";
    }

    @PostMapping("/{presetProfileImageId}/delete")
    public String delete(
            @PathVariable Long presetProfileImageId,
            RedirectAttributes redirectAttributes
    ) {
        presetProfileImageAdminService.delete(presetProfileImageId);
        redirectAttributes.addFlashAttribute("message", "프리셋 프로필 이미지가 삭제되었습니다.");
        return "redirect:/admin/preset-profile-images";
    }
}
