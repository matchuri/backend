package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupMenuAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMenuActionRepository extends JpaRepository<GroupMenuAction, Long>,
        GroupMenuActionRepositoryCustom {
}
