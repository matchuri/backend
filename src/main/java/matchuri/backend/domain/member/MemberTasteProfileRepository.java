package matchuri.backend.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTasteProfileRepository extends JpaRepository<MemberTasteProfile, Long> {

    Optional<MemberTasteProfile> findByMemberId(Long memberId);
}
