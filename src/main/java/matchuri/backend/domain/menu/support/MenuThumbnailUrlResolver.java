package matchuri.backend.domain.menu.support;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.image.support.ImageUrlResolver;
import matchuri.backend.domain.menu.entity.MenuItemImage;
import matchuri.backend.domain.menu.repository.MenuItemImageRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuThumbnailUrlResolver {

    private final MenuItemImageRepository menuItemImageRepository;
    private final ImageUrlResolver imageUrlResolver;

    public String resolve(Long menuId) {
        return menuItemImageRepository.findByMenuId(menuId)
                .map(MenuItemImage::getImageAsset)
                .map(imageAsset -> imageUrlResolver.toPublicUrl(imageAsset.getObjectKey()))
                .orElse(null);
    }

    public Map<Long, String> resolveAll(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctMenuIds = menuIds.stream()
                .distinct()
                .toList();

        return menuItemImageRepository.findAllByMenuIdIn(distinctMenuIds)
                .stream()
                .collect(Collectors.toMap(
                        menuItemImage -> menuItemImage.getMenu().getId(),
                        menuItemImage -> imageUrlResolver.toPublicUrl(menuItemImage.getImageAsset().getObjectKey()),
                        (left, right) -> left
                ));
    }
}
