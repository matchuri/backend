package matchuri.backend.domain.auth.repository;

import matchuri.backend.domain.auth.entity.OAuthUnlinkJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthUnlinkJobRepository extends JpaRepository<OAuthUnlinkJob, Integer> {
}
