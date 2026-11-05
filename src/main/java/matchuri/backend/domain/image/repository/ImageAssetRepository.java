package matchuri.backend.domain.image.repository;

import java.util.Optional;
import matchuri.backend.domain.image.entity.ImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {

    Optional<ImageAsset> findByObjectKey(String objectKey);
}
