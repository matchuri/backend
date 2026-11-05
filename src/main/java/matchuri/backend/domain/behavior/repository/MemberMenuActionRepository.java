package matchuri.backend.domain.behavior.repository;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.behavior.entity.ActionType;
import matchuri.backend.domain.behavior.entity.MemberMenuAction;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MemberMenuActionRepository extends JpaRepository<MemberMenuAction, Long> {
    List<MemberMenuAction> findByMemberIdAndActionTypeAndCreatedAtAfter(
            Long memberId,
            ActionType actionType,
            LocalDateTime createdAt
    );
}
