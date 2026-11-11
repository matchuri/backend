package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class UnlinkClientEventListenerTest {

    @Mock
    private OAuth2Unlinker unlinker;

    @InjectMocks
    private UnlinkClientEventListener listener;

    @Test
    @DisplayName("이벤트를 받으면 해당 회원의 연결 해제를 위임한다")
    void delegatesToUnlinker() {
        listener.handle(new OAuthUnlinkEvent(1L));

        verify(unlinker).unlink(1L);
    }

    @Test
    @DisplayName("외부 네트워크 오류는 리스너 밖으로 전파되지 않는다")
    void swallowsExternalFailure() {
        doThrow(new ResourceAccessException("timeout")).when(unlinker).unlink(1L);

        assertThatCode(() -> listener.handle(new OAuthUnlinkEvent(1L))).doesNotThrowAnyException();
    }
}
