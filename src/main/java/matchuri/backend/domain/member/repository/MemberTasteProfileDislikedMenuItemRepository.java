package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@NullMarked
public interface MemberTasteProfileDislikedMenuItemRepository extends
        JpaRepository<MemberTasteProfileDislikedMenuItem, Long> {

    List<MemberTasteProfileDislikedMenuItem> findAllByProfileId(Long profileId);

    @Query("""
            select mapping
            from MemberTasteProfileDislikedMenuItem mapping
            join fetch mapping.menuItem menuItem
            where mapping.profile.id = :profileId
            order by menuItem.name asc, menuItem.id asc
            """)
    List<MemberTasteProfileDislikedMenuItem> findAllByProfileIdOrderByDisplay(@Param("profileId") Long profileId);
}
