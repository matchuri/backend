package matchuri.backend.domain.group.repository;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    List<GroupInvite> findAllByRoomIdAndStatus(Long roomId, GroupInviteStatus status);

    boolean existsByRoomIdAndTargetMemberIdAndStatus(Long roomId, Long targetMemberId, GroupInviteStatus status);

    @Query(
            value = """
                    select invite
                    from GroupInvite invite
                    join fetch invite.room room
                    join fetch invite.requestMember requestMember
                    where invite.targetMember.id = :targetMemberId
                      and (:status is null or invite.status = :status)
                    order by invite.createdAt desc, invite.id desc
                    """,
            countQuery = """
                    select count(invite)
                    from GroupInvite invite
                    where invite.targetMember.id = :targetMemberId
                      and (:status is null or invite.status = :status)
                    """
    )
    Page<GroupInvite> findMyInvites(
            @Param("targetMemberId") Long targetMemberId,
            @Param("status") GroupInviteStatus status,
            Pageable pageable
    );
}
