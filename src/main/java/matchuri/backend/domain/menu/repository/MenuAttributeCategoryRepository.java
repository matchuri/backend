package matchuri.backend.domain.menu.repository;

import java.util.List;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO: 의도가 명확한 메서드명으로 변경하기
@NullMarked
public interface MenuAttributeCategoryRepository extends JpaRepository<MenuAttributeCategory, Long> {

    List<MenuAttributeCategory> findAllByMenuIdAndAttributeCategoryActiveTrueOrderByAttributeCategoryCategoryTypeAscAttributeCategorySortOrderAscAttributeCategoryIdAsc(
            Long menuId
    );
}
