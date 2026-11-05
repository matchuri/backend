package matchuri.backend.domain.group.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendation;

public interface GroupRecommendationRepositoryCustom {

    List<GroupRecommendationStatusQueryRow> findLatestStatusesByRoomIds(Collection<Long> roomIds);

    List<GroupRecommendation> findHistoryForActiveMember(Long memberId);
}
