package matchuri.backend.domain.behavior.repository;

import matchuri.backend.domain.behavior.entity.MemberMenuAction;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MemberMenuActionRepository extends JpaRepository<MemberMenuAction, Long> {
}
