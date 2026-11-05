package matchuri.backend.testsupport;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.repository.GroupRecommendationRepository;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Persists audited entities at a test-controlled creation time.
 *
 * <p>Production entities keep their immutable JPA auditing contract while integration tests can
 * express time-based policies without duplicating native SQL and persistence-context repair.
 */
@TestComponent
public class JpaAuditTimeFixture {

    private final GroupRecommendationRepository groupRecommendationRepository;
    private final JdbcTemplate jdbcTemplate;

    public JpaAuditTimeFixture(
            GroupRecommendationRepository groupRecommendationRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.groupRecommendationRepository = groupRecommendationRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public GroupRecommendation persistGroupRecommendationAt(
            GroupRecommendation recommendation,
            LocalDateTime createdAt
    ) {
        GroupRecommendation persisted = groupRecommendationRepository.saveAndFlush(recommendation);
        int updated = jdbcTemplate.update(
                "update group_recommendations set created_at = ? where id = ?",
                createdAt,
                persisted.getId()
        );
        if (updated != 1) {
            throw new IllegalStateException("Expected one group recommendation timestamp update");
        }
        ReflectionTestUtils.setField(persisted, "createdAt", createdAt);
        return persisted;
    }
}
