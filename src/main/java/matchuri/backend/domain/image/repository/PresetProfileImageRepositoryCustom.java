package matchuri.backend.domain.image.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.image.entity.PresetProfileImage;

public interface PresetProfileImageRepositoryCustom {

    List<PresetProfileImage> findAllActive();

    Optional<PresetProfileImage> findActiveById(Long presetProfileImageId);

    List<PresetProfileImage> findActiveDefaults();

    Optional<PresetProfileImage> findByObjectKey(String objectKey);

    List<PresetProfileImage> lockAllActive();
}
