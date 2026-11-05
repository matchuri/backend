package matchuri.backend.api.menu;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.menu.dto.response.MenuImageResponse;
import matchuri.backend.domain.menu.service.MenuImageAdminService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/menu-items")
public class MenuImageAdminController {

    private final MenuImageAdminService menuImageAdminService;

    @PostMapping(value = "/{menuItemId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MenuImageResponse> uploadPrimaryMenuImage(
            @PathVariable Long menuItemId,
            @RequestPart("file") MultipartFile file
    ) {
        var result = menuImageAdminService.uploadPrimaryImage(menuItemId, file);

        return ApiResponse.success(MenuImageResponse.from(result));
    }

    @DeleteMapping("/{menuItemId}/images/primary")
    public ApiResponse<Void> deletePrimaryMenuImage(@PathVariable Long menuItemId) {
        menuImageAdminService.deletePrimaryImage(menuItemId);

        return ApiResponse.successWithoutData();
    }
}
