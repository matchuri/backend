package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.menu.entity.MenuItem;
import org.jspecify.annotations.Nullable;

public interface MenuItemRepositoryCustom {

    Optional<MenuItemDetailRow> findActiveDetailRowById(Long menuItemId);

    List<MenuRecommendationRow> findActiveRecommendationRows();

    List<MenuItem> searchActiveMenuItems(@Nullable String query, Collection<Long> attributeCategoryIds, boolean attributeCategoryIdsEmpty, Collection<Long> ingredientIds, boolean ingredientIdsEmpty);
}
