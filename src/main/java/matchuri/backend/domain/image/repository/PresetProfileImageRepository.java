package matchuri.backend.domain.image.repository;

import matchuri.backend.domain.image.entity.PresetProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresetProfileImageRepository extends JpaRepository<PresetProfileImage, Long>,
        PresetProfileImageRepositoryCustom {
}
