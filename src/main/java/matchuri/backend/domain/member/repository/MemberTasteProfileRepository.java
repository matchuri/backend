package matchuri.backend.domain.member.repository;

import java.util.Optional;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTasteProfileRepository extends JpaRepository<MemberTasteProfile, Long> {

    Optional<MemberTasteProfile> findByMemberId(Long memberId);
}
