package matchuri.backend.catalog.api.query;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;
import matchuri.backend.catalog.entity.MenuItem;

public interface CatalogMenuQuery {
    List<MenuItem> findAll();
    List<MenuItem> findAllByIdInAndActiveTrue(Collection<Long> ids);
    List<MenuRecommendationRow> findActiveRecommendationRows();
    List<MenuItem> searchActiveMenuItems(@Nullable String query, Collection<Long> categoryIds, boolean categoriesEmpty, Collection<Long> ingredientIds, boolean ingredientsEmpty);
}
