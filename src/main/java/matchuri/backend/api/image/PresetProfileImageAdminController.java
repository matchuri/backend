package matchuri.backend.api.image;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.image.dto.response.PresetProfileImageResponse;
import matchuri.backend.domain.image.service.PresetProfileImageAdminService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/preset-profile-images")
public class PresetProfileImageAdminController implements PresetProfileImageAdminApi {

    private final PresetProfileImageAdminService presetProfileImageAdminService;

    @Override
    @GetMapping
    public ApiResponse<List<PresetProfileImageResponse>> getPresetProfileImages() {
        List<PresetProfileImageResponse> response = presetProfileImageAdminService.getPresetProfileImages().stream()
                .map(PresetProfileImageResponse::from)
                .toList();
        return ApiResponse.success(response);
    }

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PresetProfileImageResponse> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(PresetProfileImageResponse.from(presetProfileImageAdminService.upload(file)));
    }

    @Override
    @DeleteMapping("/{presetProfileImageId}")
    public ApiResponse<Void> delete(@PathVariable Long presetProfileImageId) {
        presetProfileImageAdminService.delete(presetProfileImageId);
        return ApiResponse.successWithoutData();
    }

    @Override
    @PutMapping("/{presetProfileImageId}/default")
    public ApiResponse<PresetProfileImageResponse> setDefault(@PathVariable Long presetProfileImageId) {
        return ApiResponse.success(PresetProfileImageResponse.from(
                presetProfileImageAdminService.setDefault(presetProfileImageId)
        ));
    }
}
