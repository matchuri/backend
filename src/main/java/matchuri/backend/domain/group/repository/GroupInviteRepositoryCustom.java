package matchuri.backend.domain.group.repository;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupInviteRepositoryCustom {

    Page<GroupInvite> findMyInvites(Long targetMemberId, @Nullable GroupInviteStatus status, LocalDateTime now, Pageable pageable);
}
