package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberTasteProfileCategoryRepository extends JpaRepository<MemberTasteProfileCategory, Long> {

    @Query("""
            select mapping
            from MemberTasteProfileCategory mapping
            join fetch mapping.attributeCategory attributeCategory
            where mapping.profile.id = :profileId
            order by attributeCategory.categoryType asc, attributeCategory.sortOrder asc, attributeCategory.id asc
            """)
    List<MemberTasteProfileCategory> findAllByProfileIdOrderByDisplay(@Param("profileId") Long profileId);
}
