package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeCategoryRepository extends JpaRepository<AttributeCategory, Long> {

    boolean existsByCategoryTypeAndCode(CategoryType categoryType, String code);

    List<AttributeCategory> findAllByOrderByCategoryTypeAscSortOrderAscIdAsc();

    List<AttributeCategory> findAllByActiveTrueOrderByCategoryTypeAscSortOrderAscIdAsc();

    List<AttributeCategory> findAllByIdInAndActiveTrue(Collection<Long> ids);
}
