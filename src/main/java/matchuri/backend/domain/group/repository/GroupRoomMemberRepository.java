package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRoomMemberRepository extends JpaRepository<GroupRoomMember, Long> {

    @Query(
            value = """
                    select groupMember
                    from GroupRoomMember groupMember
                    join fetch groupMember.room room
                    where groupMember.member.id = :memberId
                      and groupMember.status = matchuri.backend.domain.group.entity.GroupMemberStatus.ACTIVE
                      and room.status <> matchuri.backend.domain.group.entity.GroupRoomStatus.DELETED
                      and (:roomStatus is null or room.status = :roomStatus)
                    order by room.createdAt desc, room.id desc
                    """,
            countQuery = """
                    select count(groupMember)
                    from GroupRoomMember groupMember
                    join groupMember.room room
                    where groupMember.member.id = :memberId
                      and groupMember.status = matchuri.backend.domain.group.entity.GroupMemberStatus.ACTIVE
                      and room.status <> matchuri.backend.domain.group.entity.GroupRoomStatus.DELETED
                      and (:roomStatus is null or room.status = :roomStatus)
                    """
    )
    Page<GroupRoomMember> findMyActiveMemberships(
            @Param("memberId") Long memberId,
            @Param("roomStatus") GroupRoomStatus roomStatus,
            Pageable pageable
    );

    @Query("""
            select groupMember.room.id as roomId, count(groupMember) as memberCount
            from GroupRoomMember groupMember
            where groupMember.room.id in :roomIds
              and groupMember.status = :memberStatus
            group by groupMember.room.id
            """)
    List<GroupRoomMemberCountProjection> countMembersByRoomIdsAndStatus(
            @Param("roomIds") List<Long> roomIds,
            @Param("memberStatus") GroupMemberStatus memberStatus
    );

    @Query("""
            select count(groupMember) > 0
            from GroupRoomMember groupMember
            join groupMember.room room
            where room.id = :roomId
              and room.status <> matchuri.backend.domain.group.entity.GroupRoomStatus.DELETED
              and groupMember.member.id = :memberId
              and groupMember.status = matchuri.backend.domain.group.entity.GroupMemberStatus.ACTIVE
            """)
    boolean existsActiveMembershipInNotDeletedRoom(
            @Param("roomId") Long roomId,
            @Param("memberId") Long memberId
    );

    @Query("""
            select groupMember
            from GroupRoomMember groupMember
            join groupMember.room room
            where room.id = :roomId
              and room.status <> matchuri.backend.domain.group.entity.GroupRoomStatus.DELETED
              and groupMember.member.id = :memberId
              and groupMember.status = matchuri.backend.domain.group.entity.GroupMemberStatus.ACTIVE
            """)
    Optional<GroupRoomMember> findActiveMembershipInNotDeletedRoom(
            @Param("roomId") Long roomId,
            @Param("memberId") Long memberId
    );

    @Query("""
            select groupMember
            from GroupRoomMember groupMember
            join fetch groupMember.member member
            where groupMember.room.id = :roomId
              and groupMember.status = matchuri.backend.domain.group.entity.GroupMemberStatus.ACTIVE
            order by
              case
                when groupMember.role = matchuri.backend.domain.group.entity.GroupMemberRole.OWNER then 0
                else 1
              end,
              groupMember.joinedAt asc,
              groupMember.id asc
            """)
    List<GroupRoomMember> findActiveMembersByRoomId(@Param("roomId") Long roomId);
}
