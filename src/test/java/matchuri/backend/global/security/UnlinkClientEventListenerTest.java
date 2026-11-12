package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.net.SocketTimeoutException;
import matchuri.backend.domain.auth.entity.FailureCode;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.service.OAuthUnlinkJobServiceImpl;
import matchuri.backend.global.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class UnlinkClientEventListenerTest {

    @Mock
    private OAuth2Unlinker unlinker;

    @Mock
    private OAuthUnlinkJobServiceImpl jobService;

    private UnlinkClientEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new UnlinkClientEventListener(unlinker, new OAuthUnlinkFailureHandler(jobService));
    }

    @Test
    @DisplayName("이벤트를 받으면 해당 회원의 연결 해제를 위임하고 성공하면 기록하지 않는다")
    void delegatesToUnlinker() {
        listener.handle(new OAuthUnlinkEvent(1L));

        verify(unlinker).unlink(1L);
        verifyNoInteractions(jobService);
    }

    @Test
    @DisplayName("외부 호출 실패는 실패 기록으로 남기고 리스너 밖으로 전파하지 않는다")
    void recordsExternalFailure() {
        ResourceAccessException failure = new ResourceAccessException("timeout", new SocketTimeoutException());
        doThrow(failure).when(unlinker).unlink(1L);

        assertThatCode(() -> listener.handle(new OAuthUnlinkEvent(1L))).doesNotThrowAnyException();

        verify(jobService).recordFailureJob(1L, FailureCode.TIMEOUT, null);
    }

    @Test
    @DisplayName("실패 기록 저장이 실패해도 예외는 전파되지 않는다")
    void swallowsRecordFailure() {
        doThrow(new ResourceAccessException("timeout", new SocketTimeoutException())).when(unlinker).unlink(1L);
        doThrow(new IllegalStateException("db down")).when(jobService).recordFailureJob(1L, FailureCode.TIMEOUT, null);

        assertThatCode(() -> listener.handle(new OAuthUnlinkEvent(1L))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("재시도로 해결되지 않는 도메인 예외는 기록하지 않고 전파하지도 않는다")
    void doesNotRecordNonRetryableFailure() {
        doThrow(new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED))
                .when(unlinker).unlink(1L);

        assertThatCode(() -> listener.handle(new OAuthUnlinkEvent(1L))).doesNotThrowAnyException();

        verifyNoInteractions(jobService);
    }

    @Test
    @DisplayName("일시적 장애 대상에 포함되지 않는 HTTP 오류는 작업을 생성하지 않는다")
    void doesNotRecordHttpClientFailure() {
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(unlinker).unlink(1L);

        assertThatCode(() -> listener.handle(new OAuthUnlinkEvent(1L))).doesNotThrowAnyException();

        verifyNoInteractions(jobService);
    }
}
