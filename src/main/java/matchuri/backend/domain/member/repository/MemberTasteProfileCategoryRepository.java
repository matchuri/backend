package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTasteProfileCategoryRepository extends JpaRepository<MemberTasteProfileCategory, Long>,
        MemberTasteProfileCategoryRepositoryCustom {

    List<MemberTasteProfileCategory> findAllByProfileId(Long profileId);

}
