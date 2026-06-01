package matchuri.backend.domain.menu.service;

import matchuri.backend.domain.menu.result.AdminMenuItemDetailResult;
import matchuri.backend.domain.menu.result.MenuImageResult;
import org.springframework.web.multipart.MultipartFile;

public interface MenuImageAdminService {

    AdminMenuItemDetailResult getAdminMenuItemDetail(Long menuItemId);

    MenuImageResult uploadPrimaryImage(Long menuItemId, MultipartFile file);

    void deletePrimaryImage(Long menuItemId);
}
