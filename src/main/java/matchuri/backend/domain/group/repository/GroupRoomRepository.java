package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRoomRepository extends JpaRepository<GroupRoom, Long> {

    Optional<GroupRoom> findByIdAndStatusNot(Long id, GroupRoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select room from GroupRoom room where room.id = :id and room.status <> :status")
    Optional<GroupRoom> findByIdAndStatusNotForUpdate(
            @Param("id") Long id,
            @Param("status") GroupRoomStatus status
    );

    boolean existsByInviteCode(String inviteCode);

    Optional<GroupRoom> findByInviteCode(String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select room from GroupRoom room
            where room.hostMember.id = :memberId
              and room.status <> matchuri.backend.domain.group.entity.GroupRoomStatus.DELETED
            """)
    List<GroupRoom> findOwnedNotDeletedForUpdate(@Param("memberId") Long memberId);
}
