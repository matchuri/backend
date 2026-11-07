package matchuri.backend.domain.image.service;

import java.util.List;
import matchuri.backend.domain.image.result.PresetProfileImageResult;
import org.springframework.web.multipart.MultipartFile;

public interface PresetProfileImageAdminService {

    List<PresetProfileImageResult> getPresetProfileImages();

    PresetProfileImageResult upload(MultipartFile file);

    PresetProfileImageResult setDefault(Long presetProfileImageId);

    void delete(Long presetProfileImageId);
}
