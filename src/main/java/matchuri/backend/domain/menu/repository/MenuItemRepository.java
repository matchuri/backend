package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.menu.entity.MenuItem;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, MenuItemRepositoryCustom {

    boolean existsByCode(String code);

    Optional<MenuItem> findByCode(String code);

    List<MenuItem> findAllByOrderByIdAsc();

    List<MenuItem> findAllByIdInAndActiveTrue(Collection<Long> ids);

}
