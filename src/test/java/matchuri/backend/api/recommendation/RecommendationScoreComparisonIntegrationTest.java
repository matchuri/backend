package matchuri.backend.api.recommendation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationCandidateGenerator;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:recommendation_score_comparison;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("test")
@Transactional
class RecommendationScoreComparisonIntegrationTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private GroupRecommendationCandidateGenerator generator;

    @Test
    void printGroupScores() {
        var categories = List.of(
                new AttributeCategory(CategoryType.FOOD_CATEGORY, "KOREAN", "한식", 1),
                new AttributeCategory(CategoryType.FOOD_CATEGORY, "CHINESE", "중식", 2),
                new AttributeCategory(CategoryType.TEMPERATURE, "HOT", "뜨거움", 3),
                new AttributeCategory(CategoryType.TEMPERATURE, "COLD", "차가움", 4),
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매콤", 5),
                new AttributeCategory(CategoryType.FLAVOR, "RICH", "진한 맛", 6),
                new AttributeCategory(CategoryType.COOKING_METHOD, "SOUP", "국물", 7),
                new AttributeCategory(CategoryType.COOKING_METHOD, "GRILLED", "구이", 8),
                new AttributeCategory(CategoryType.TEXTURE, "CRISPY", "바삭", 9),
                new AttributeCategory(CategoryType.TEXTURE, "CHEWY", "쫄깃", 10));
        categories.forEach(entityManager::persist);
        var menu = new MenuItem("KOREAN_HOT", "한식·뜨거움", "점수 비교용");
        entityManager.persist(menu);
        for (int index : List.of(0, 2, 4, 5, 6, 8)) {
            entityManager.persist(new MenuAttributeCategory(menu, categories.get(index)));
        }
        var names = List.of("EXCLUSIVE", "MIXED", "OTHER_TYPES");
        var preferences = List.of(List.of(0, 1, 2, 3), List.of(0, 1, 2, 3, 4, 5), List.of(4, 5, 6, 7, 8, 9));
        for (int scenario = 0; scenario < names.size(); scenario++) {
            var members = new ArrayList<Member>();
            for (int number = 0; number < 3; number++) {
                String loginId = names.get(scenario) + number;
                var member = Member.createWithEncodedPassword(loginId, "test-password", loginId, null);
                entityManager.persist(member);
                var profile = new MemberTasteProfile(member, "v1");
                entityManager.persist(profile);
                for (int index : preferences.get(scenario)) {
                    entityManager.persist(new MemberTasteProfileCategory(profile, categories.get(index)));
                }
                members.add(member);
            }
            var room = GroupRoom.createOwnedBy(names.get(scenario), names.get(scenario), members.getFirst());
            room.addGroupMember(members.get(1), GroupMemberRole.MEMBER);
            room.addGroupMember(members.get(2), GroupMemberRole.MEMBER);
            entityManager.persist(room);
            // Reload persisted category relationships for the actual candidate generation path.
            entityManager.flush();
            entityManager.clear();
            room = entityManager.find(GroupRoom.class, room.getId());
            var recommendation = GroupRecommendation.preparing(room);
            entityManager.persist(recommendation);
            var candidate = generator.generateCandidatesForRecommendation(room, recommendation, "{}", List.of()).getFirst();
            System.out.printf(Locale.ROOT, "SCORE_COMPARISON %s groupScore=%.1f%n", names.get(scenario), candidate.getScore());
        }
    }
}
