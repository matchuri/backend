package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;

public interface MenuAttributeCategoryRepositoryCustom {

    List<MenuAttributeCategory> findDisplayCategoriesByMenuIds(List<Long> menuIds);

    List<MenuAttributeCategoryRow> findActiveRowsByMenuId(Long menuId);

    List<MenuAttributeCategoryIdRow> findIdRowsByMenuIds(Collection<Long> menuIds);
}
