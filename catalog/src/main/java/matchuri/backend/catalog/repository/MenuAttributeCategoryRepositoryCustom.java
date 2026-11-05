package matchuri.backend.catalog.repository;

import matchuri.backend.catalog.api.query.MenuAttributeCategoryIdRow;

import java.util.Collection;
import java.util.List;
import matchuri.backend.catalog.entity.MenuAttributeCategory;

public interface MenuAttributeCategoryRepositoryCustom {

    List<MenuAttributeCategory> findDisplayCategoriesByMenuIds(List<Long> menuIds);

    List<MenuAttributeCategoryRow> findActiveRowsByMenuId(Long menuId);

    List<MenuAttributeCategoryIdRow> findIdRowsByMenuIds(Collection<Long> menuIds);
}
