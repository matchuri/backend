package matchuri.backend.catalog.repository;

import java.util.List;
import matchuri.backend.catalog.entity.AttributeCategory;
import matchuri.backend.catalog.entity.MenuAttributeCategory;
import matchuri.backend.catalog.entity.MenuItem;
import org.jspecify.annotations.NullMarked;
import matchuri.backend.catalog.api.query.CatalogMenuAttributeQuery;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO: 의도가 명확한 메서드명으로 변경하기
@NullMarked
public interface MenuAttributeCategoryRepository extends JpaRepository<MenuAttributeCategory, Long>, CatalogMenuAttributeQuery,
        MenuAttributeCategoryRepositoryCustom {

    boolean existsByMenuAndAttributeCategory(MenuItem menu, AttributeCategory attributeCategory);

    List<MenuAttributeCategory> findAllByMenuIdOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
            Long menuId
    );

    List<MenuAttributeCategory> findAllByMenuIdAndAttributeCategoryActiveTrueOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
            Long menuId
    );
}
