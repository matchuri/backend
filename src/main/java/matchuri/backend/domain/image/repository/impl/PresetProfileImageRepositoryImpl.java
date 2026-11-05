package matchuri.backend.domain.image.repository.impl;

import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.image.entity.QPresetProfileImage.presetProfileImage;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.image.entity.PresetProfileImage;
import matchuri.backend.domain.image.repository.PresetProfileImageRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PresetProfileImageRepositoryImpl implements PresetProfileImageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PresetProfileImage> findAllActive() {
        return activePresets()
                .orderBy(presetProfileImage.id.asc())
                .fetch();
    }

    @Override
    public Optional<PresetProfileImage> findActiveById(Long presetProfileImageId) {
        return Optional.ofNullable(
                activePresets()
                        .where(presetProfileImage.id.eq(presetProfileImageId))
                        .fetchOne()
        );
    }

    @Override
    public List<PresetProfileImage> findActiveDefaults() {
        return activePresets()
                .where(presetProfileImage.isDefault.isTrue())
                .fetch();
    }

    @Override
    public Optional<PresetProfileImage> findByObjectKey(String objectKey) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(presetProfileImage)
                        .join(presetProfileImage.imageAsset, imageAsset).fetchJoin()
                        .where(imageAsset.objectKey.eq(objectKey))
                        .fetchOne()
        );
    }

    @Override
    public List<PresetProfileImage> lockAllActive() {
        return activePresets()
                .orderBy(presetProfileImage.id.asc())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }

    private JPAQuery<PresetProfileImage> activePresets() {
        return jpaQueryFactory
                .selectFrom(presetProfileImage)
                .join(presetProfileImage.imageAsset, imageAsset).fetchJoin()
                .where(presetProfileImage.isDeleted.isFalse());
    }
}
