package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupRoom.groupRoom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupRoomRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRoomRepositoryImpl implements GroupRoomRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<GroupRoom> findByIdAndStatusNotForUpdate(Long id, GroupRoomStatus status) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(groupRoom)
                        .where(
                                groupRoom.id.eq(id),
                                groupRoom.status.ne(status)
                        )
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

    @Override
    public List<GroupRoom> findOwnedNotDeletedForUpdate(Long memberId) {
        return jpaQueryFactory
                .selectFrom(groupRoom)
                .where(
                        groupRoom.hostMember.id.eq(memberId),
                        groupRoom.status.ne(GroupRoomStatus.DELETED)
                )
                .orderBy(groupRoom.id.asc())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }
}
