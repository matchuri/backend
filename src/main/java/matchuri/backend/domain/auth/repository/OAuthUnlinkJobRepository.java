package matchuri.backend.domain.auth.repository;

import java.util.Optional;
import matchuri.backend.domain.auth.entity.OAuthUnlinkJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthUnlinkJobRepository extends JpaRepository<OAuthUnlinkJob, Long>, OAuthUnlinkJobRepositoryCustom {

    Optional<OAuthUnlinkJob> findByMemberId(Long memberId);
}
