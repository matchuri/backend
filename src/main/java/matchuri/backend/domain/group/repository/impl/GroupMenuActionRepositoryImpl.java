package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupMenuAction.groupMenuAction;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupMenuActionType;
import matchuri.backend.domain.group.repository.GroupMenuActionRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupMenuActionRepositoryImpl implements GroupMenuActionRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Long> findMenuItemIdsByGroupRoomIdAndActionTypeAndCreatedAtAfter(Long groupRoomId, GroupMenuActionType actionType, LocalDateTime createdAt) {
        return jpaQueryFactory
                .select(groupMenuAction.menuItem.id)
                .distinct()
                .from(groupMenuAction)
                .where(
                        groupMenuAction.groupRoom.id.eq(groupRoomId),
                        groupMenuAction.actionType.eq(actionType),
                        groupMenuAction.createdAt.goe(createdAt)
                )
                .fetch();
    }
}
