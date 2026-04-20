package matchuri.backend.api.menu;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.menu.dto.request.CreateAdminAttributeCategoryRequest;
import matchuri.backend.api.menu.dto.request.UpdateAdminAttributeCategoryRequest;
import matchuri.backend.api.menu.dto.response.AdminAttributeCategoryResponse;
import matchuri.backend.api.menu.mapper.MenuReferenceMapper;
import matchuri.backend.domain.menu.service.MenuAdminReferenceService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

        var results = menuAdminReferenceService.getAttributeCategories();
        var responses = menuReferenceMapper.toAdminAttributeCategoryResponses(results);

        return ApiResponse.success(responses);
    }

    @Override
    @PostMapping("/attribute-categories")
    public ApiResponse<AdminAttributeCategoryResponse> createAdminAttributeCategory(
            @Valid @RequestBody CreateAdminAttributeCategoryRequest request
    ) {

        var command = menuReferenceMapper.toCreateAdminAttributeCategoryCommand(request);
        var result = menuAdminReferenceService.createAttributeCategory(command);
        var response = menuReferenceMapper.toAdminAttributeCategoryResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/attribute-categories/{attributeCategoryId}")
    public ApiResponse<AdminAttributeCategoryResponse> updateAdminAttributeCategory(
            @PathVariable Long attributeCategoryId,
            @Valid @RequestBody UpdateAdminAttributeCategoryRequest request
    ) {

        var command = menuReferenceMapper.toUpdateAdminAttributeCategoryCommand(attributeCategoryId, request);
        var result = menuAdminReferenceService.updateAttributeCategory(command);
        var response = menuReferenceMapper.toAdminAttributeCategoryResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @DeleteMapping("/attribute-categories/{attributeCategoryId}")
    public ApiResponse<AdminAttributeCategoryResponse> deactivateAdminAttributeCategory(
            @PathVariable Long attributeCategoryId
    ) {
        var result = menuAdminReferenceService.deactivateAttributeCategory(attributeCategoryId);
        var response = menuReferenceMapper.toAdminAttributeCategoryResponse(result);

        return ApiResponse.success(response);
    }
}
