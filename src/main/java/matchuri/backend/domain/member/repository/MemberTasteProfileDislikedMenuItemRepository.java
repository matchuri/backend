package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MemberTasteProfileDislikedMenuItemRepository extends
        JpaRepository<MemberTasteProfileDislikedMenuItem, Long>,
        MemberTasteProfileDislikedMenuItemRepositoryCustom {

    List<MemberTasteProfileDislikedMenuItem> findAllByProfileId(Long profileId);

}
