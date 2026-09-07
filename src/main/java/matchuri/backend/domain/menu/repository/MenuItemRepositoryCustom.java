package matchuri.backend.domain.menu.repository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepositoryCustom {

    Optional<MenuItemDetailQueryResult> findActiveMenuItemDetailById(Long menuItemId);

    List<MenuRecommendationProfileQueryResult> findActiveMenuRecommendationProfiles();
}
