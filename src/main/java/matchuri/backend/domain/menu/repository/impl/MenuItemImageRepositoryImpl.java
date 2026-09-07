package matchuri.backend.domain.menu.repository.impl;

import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.menu.entity.QMenuItemImage.menuItemImage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.repository.MenuItemImageRepositoryCustom;
import matchuri.backend.domain.menu.repository.MenuThumbnailQueryRow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MenuItemImageRepositoryImpl implements MenuItemImageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MenuThumbnailQueryRow> findThumbnailRowsByMenuIds(Collection<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        MenuThumbnailQueryRow.class,
                        menuItemImage.menu.id,
                        imageAsset.objectKey
                ))
                .from(menuItemImage)
                .join(menuItemImage.imageAsset, imageAsset)
                .where(menuItemImage.menu.id.in(menuIds))
                .fetch();
    }
}
