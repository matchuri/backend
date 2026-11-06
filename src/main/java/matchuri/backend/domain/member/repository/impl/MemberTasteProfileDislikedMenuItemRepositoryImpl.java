package matchuri.backend.domain.member.repository.impl;

import static matchuri.backend.domain.member.entity.QMemberTasteProfileDislikedMenuItem.memberTasteProfileDislikedMenuItem;
import static matchuri.backend.domain.menu.entity.QMenuItem.menuItem;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import matchuri.backend.domain.member.repository.MemberTasteProfileDislikedMenuItemRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberTasteProfileDislikedMenuItemRepositoryImpl
        implements MemberTasteProfileDislikedMenuItemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberTasteProfileDislikedMenuItem> findAllByProfileIdOrderByDisplay(Long profileId) {
        return jpaQueryFactory
                .selectFrom(memberTasteProfileDislikedMenuItem)
                .join(memberTasteProfileDislikedMenuItem.menuItem, menuItem).fetchJoin()
                .where(memberTasteProfileDislikedMenuItem.profile.id.eq(profileId))
                .orderBy(menuItem.name.asc(), menuItem.id.asc())
                .fetch();
    }
}
