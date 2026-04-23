package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    boolean existsByCode(String code);

    List<MenuItem> findAllByIdInAndActiveTrue(Collection<Long> ids);
}
