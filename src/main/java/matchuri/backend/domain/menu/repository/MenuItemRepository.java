package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.menu.entity.MenuItem;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@NullMarked
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    boolean existsByCode(String code);

    List<MenuItem> findAllByIdInAndActiveTrue(Collection<Long> ids);

    @Query("""
            select distinct menu
            from MenuItem menu
            where menu.active = true
              and (:query is null or lower(menu.name) like lower(concat('%', :query, '%')))
              and (
                    :attributeCategoryIdsEmpty = true
                    or exists (
                        select 1
                        from MenuAttributeCategory menuAttributeCategory
                        where menuAttributeCategory.menu = menu
                          and menuAttributeCategory.attributeCategory.active = true
                          and menuAttributeCategory.attributeCategory.id in :attributeCategoryIds
                    )
                  )
              and (
                    :ingredientIdsEmpty = true
                    or exists (
                        select 1
                        from MenuIngredient menuIngredient
                        where menuIngredient.menu = menu
                          and menuIngredient.ingredient.active = true
                          and menuIngredient.ingredient.id in :ingredientIds
                    )
                  )
            order by menu.id asc
            """)
    List<MenuItem> searchActiveMenuItems(
            @Param("query") String query,
            @Param("attributeCategoryIds") Collection<Long> attributeCategoryIds,
            @Param("attributeCategoryIdsEmpty") boolean attributeCategoryIdsEmpty,
            @Param("ingredientIds") Collection<Long> ingredientIds,
            @Param("ingredientIdsEmpty") boolean ingredientIdsEmpty
    );
}
