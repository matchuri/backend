package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * AFTER_COMMIT + 비동기 배선 검증. 클래스 레벨 @Transactional을 쓰면 커밋이 일어나지 않으므로 사용하지 않는다.
 */
@SpringBootTest
@ActiveProfiles("test")
class UnlinkClientEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private OAuth2Unlinker unlinker;

    @Test
    @DisplayName("트랜잭션이 커밋되면 연결 해제가 비동기로 실행되고, 실패해도 커밋 결과에 영향이 없다")
    void unlinksAfterCommitEvenWhenUnlinkFails() {
        doThrow(new IllegalStateException("provider down")).when(unlinker).unlink(10L);

        new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> publisher.publishEvent(new OAuthUnlinkEvent(10L)));

        verify(unlinker, timeout(3000)).unlink(10L);
    }

    @Test
    @DisplayName("트랜잭션이 롤백되면 연결 해제를 실행하지 않는다")
    void doesNotUnlinkWhenTransactionRollsBack() {
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(new OAuthUnlinkEvent(20L));
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);

        verify(unlinker, after(500).never()).unlink(20L);
    }
}
