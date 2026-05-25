package matchuri.backend.domain.group.repository;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupMenuAction;
import matchuri.backend.domain.group.entity.GroupMenuActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMenuActionRepository extends JpaRepository<GroupMenuAction, Long> {

    @Query("""
            select distinct action.menuItem.id
            from GroupMenuAction action
            where action.groupRoom.id = :groupRoomId
              and action.actionType = :actionType
              and action.createdAt >= :createdAt
            """)
    List<Long> findMenuItemIdsByGroupRoomIdAndActionTypeAndCreatedAtAfter(
            @Param("groupRoomId") Long groupRoomId,
            @Param("actionType") GroupMenuActionType actionType,
            @Param("createdAt") LocalDateTime createdAt
    );
}
