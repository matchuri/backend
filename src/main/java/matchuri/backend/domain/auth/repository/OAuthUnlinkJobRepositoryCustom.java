package matchuri.backend.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.auth.entity.Status;
import org.springframework.data.domain.Pageable;

public interface OAuthUnlinkJobRepositoryCustom {

    List<OAuthUnlinkRetryRow> findDueJobs(Status status, LocalDateTime now, Pageable pageable);
}
