package matchuri.backend.api.menu;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.menu.dto.response.AdminAttributeCategoryResponse;
import matchuri.backend.api.menu.mapper.MenuReferenceMapper;
import matchuri.backend.domain.menu.service.MenuAdminReferenceService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class MenuAdminReferenceController implements MenuAdminReferenceApi {

    private final MenuAdminReferenceService menuAdminReferenceService;
    private final MenuReferenceMapper menuReferenceMapper;

    @Override
    @GetMapping("/attribute-categories")
    public ApiResponse<List<AdminAttributeCategoryResponse>> getAdminAttributeCategories() {
        return ApiResponse.success(
                menuReferenceMapper.toAdminAttributeCategoryResponses(
                        menuAdminReferenceService.getAttributeCategories()
                )
        );
    }
}
