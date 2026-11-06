package matchuri.backend.api;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.domain.auth.service.LocalTestAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthTestProfileIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("로컬 프로필이 아니면 테스트 로그인 bean을 등록하지 않는다")
    void excludesTestLoginBeansOutsideLocalProfile() {
        assertThat(applicationContext.getBeansOfType(AuthTestController.class)).isEmpty();
        assertThat(applicationContext.getBeansOfType(LocalTestAuthService.class)).isEmpty();
    }
}
