package matchuri.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import matchuri.backend.domain.group.service.impl.GroupInviteServiceImpl;
import matchuri.backend.domain.group.service.impl.GroupManagementServiceImpl;
import matchuri.backend.domain.group.service.impl.GroupRecommendationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;

class GroupServiceArchitectureTest {

    @Test
    @DisplayName("그룹 기능별 서비스는 정해진 Repository 의존 예산을 넘지 않는다")
    void capabilityServicesKeepRepositoryDependenciesFocused() {
        assertThat(repositoryDependencyCount(GroupManagementServiceImpl.class)).isLessThanOrEqualTo(4);
        assertThat(repositoryDependencyCount(GroupInviteServiceImpl.class)).isLessThanOrEqualTo(4);
        assertThat(repositoryDependencyCount(GroupRecommendationServiceImpl.class)).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("그룹 기능별 구현은 각 유스케이스 인터페이스만 구현한다")
    void capabilityServicesImplementSingleUseCaseInterface() {
        assertThat(GroupManagementServiceImpl.class.getInterfaces())
                .containsExactly(GroupManagementService.class);
        assertThat(GroupInviteServiceImpl.class.getInterfaces())
                .containsExactly(GroupInviteService.class);
        assertThat(GroupRecommendationServiceImpl.class.getInterfaces())
                .containsExactly(GroupRecommendationService.class);
    }

    private long repositoryDependencyCount(Class<?> serviceType) {
        return Arrays.stream(serviceType.getDeclaredFields())
                .map(Field::getType)
                .filter(Repository.class::isAssignableFrom)
                .count();
    }
}
