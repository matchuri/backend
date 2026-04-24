package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@NullMarked
public interface MemberTasteProfileRestrictionIngredientRepository extends
        JpaRepository<MemberTasteProfileRestrictionIngredient, Long> {

    List<MemberTasteProfileRestrictionIngredient> findAllByProfileId(Long profileId);

    @Query("""
            select mapping
            from MemberTasteProfileRestrictionIngredient mapping
            join fetch mapping.ingredient ingredient
            where mapping.profile.id = :profileId
            order by ingredient.sortOrder asc, ingredient.id asc
            """)
    List<MemberTasteProfileRestrictionIngredient> findAllByProfileIdOrderByDisplay(@Param("profileId") Long profileId);
}
