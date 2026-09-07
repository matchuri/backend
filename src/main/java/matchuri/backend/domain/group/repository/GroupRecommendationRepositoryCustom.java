package matchuri.backend.domain.group.repository;

import java.util.Collection;
import java.util.List;

public interface GroupRecommendationRepositoryCustom {

    List<GroupRecommendationStatusQueryRow> findLatestStatusesByRoomIds(Collection<Long> roomIds);
}
