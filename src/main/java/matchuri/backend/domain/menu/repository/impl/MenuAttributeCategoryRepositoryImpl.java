package matchuri.backend.domain.menu.repository.impl;

import static matchuri.backend.domain.menu.entity.QAttributeCategory.attributeCategory;
import static matchuri.backend.domain.menu.entity.QMenuAttributeCategory.menuAttributeCategory;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryIdRow;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepositoryCustom;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MenuAttributeCategoryRepositoryImpl implements MenuAttributeCategoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MenuAttributeCategory> findDisplayCategoriesByMenuIds(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .selectFrom(menuAttributeCategory)
                .join(menuAttributeCategory.attributeCategory, attributeCategory).fetchJoin()
                .where(
                        menuAttributeCategory.menu.id.in(menuIds),
                        attributeCategory.active.isTrue()
                )
                .orderBy(
                        attributeCategory.categoryType.asc(),
                        attributeCategory.sortOrder.asc(),
                        attributeCategory.id.asc()
                )
                .fetch();
    }

    @Override
    public List<MenuAttributeCategoryRow> findActiveRowsByMenuId(Long menuId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MenuAttributeCategoryRow.class,
                        attributeCategory.id,
                        attributeCategory.categoryType,
                        attributeCategory.code,
                        attributeCategory.name,
                        attributeCategory.sortOrder
                ))
                .from(menuAttributeCategory)
                .join(menuAttributeCategory.attributeCategory, attributeCategory)
                .where(
                        menuAttributeCategory.menu.id.eq(menuId),
                        attributeCategory.active.isTrue()
                )
                .orderBy(
                        attributeCategory.categoryType.asc(),
                        attributeCategory.sortOrder.asc(),
                        attributeCategory.id.asc()
                )
                .fetch();
    }

    @Override
    public List<MenuAttributeCategoryIdRow> findIdRowsByMenuIds(Collection<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        MenuAttributeCategoryIdRow.class,
                        menuAttributeCategory.menu.id,
                        attributeCategory.id
                ))
                .from(menuAttributeCategory)
                .join(menuAttributeCategory.attributeCategory, attributeCategory)
                .where(menuAttributeCategory.menu.id.in(menuIds))
                .orderBy(menuAttributeCategory.id.asc())
                .fetch();
    }
}
