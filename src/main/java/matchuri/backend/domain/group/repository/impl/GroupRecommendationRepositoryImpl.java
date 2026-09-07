package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupRecommendation.groupRecommendation;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.QGroupRecommendation;
import matchuri.backend.domain.group.repository.GroupRecommendationRepositoryCustom;
import matchuri.backend.domain.group.repository.GroupRecommendationStatusQueryRow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRecommendationRepositoryImpl implements GroupRecommendationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GroupRecommendationStatusQueryRow> findLatestStatusesByRoomIds(Collection<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return List.of();
        }

        QGroupRecommendation newerRecommendation = new QGroupRecommendation("newerRecommendation");

        return jpaQueryFactory
                .select(Projections.constructor(
                        GroupRecommendationStatusQueryRow.class,
                        groupRecommendation.room.id,
                        groupRecommendation.status
                ))
                .from(groupRecommendation)
                .where(
                        groupRecommendation.room.id.in(roomIds),
                        jpaQueryFactory
                                .selectOne()
                                .from(newerRecommendation)
                                .where(
                                        newerRecommendation.room.eq(groupRecommendation.room),
                                        newerRecommendation.createdAt.gt(groupRecommendation.createdAt)
                                                .or(newerRecommendation.createdAt.eq(groupRecommendation.createdAt)
                                                        .and(newerRecommendation.id.gt(groupRecommendation.id)))
                                )
                                .notExists()
                )
                .fetch();
    }
}
