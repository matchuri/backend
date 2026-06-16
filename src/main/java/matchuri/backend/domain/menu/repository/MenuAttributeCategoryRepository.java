package matchuri.backend.domain.menu.repository;

import java.util.List;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuItem;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO: 의도가 명확한 메서드명으로 변경하기
@NullMarked
public interface MenuAttributeCategoryRepository extends JpaRepository<MenuAttributeCategory, Long> {

    boolean existsByMenuAndAttributeCategory(MenuItem menu, AttributeCategory attributeCategory);

    List<MenuAttributeCategory> findAllByMenuIdOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
            Long menuId
    );

    List<MenuAttributeCategory> findAllByMenuIdAndAttributeCategoryActiveTrueOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
            Long menuId
    );
}
