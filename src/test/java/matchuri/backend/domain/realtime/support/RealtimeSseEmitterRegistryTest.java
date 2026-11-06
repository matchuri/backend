package matchuri.backend.domain.realtime.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class RealtimeSseEmitterRegistryTest {

    private final RealtimeSseEmitterRegistry registry = new RealtimeSseEmitterRegistry();

    @Test
    @DisplayName("회원과 그룹 SSE 연결 수를 조회할 수 있다")
    void countsMemberAndGroupConnections() {
        registry.registerMember(1001L);
        registry.registerMember(1001L);
        registry.registerGroup(3001L, 1001L);

        assertThat(registry.countMemberConnections(1001L)).isEqualTo(2);
        assertThat(registry.countGroupConnections(3001L)).isEqualTo(1);
        assertThat(registry.countTotalConnections()).isEqualTo(3);
    }

    @Test
    @DisplayName("단일 emitter에 연결 확인 이벤트를 전송할 수 있다")
    void sendsToSingleEmitter() {
        assertThat(registry.sendToEmitter(
                registry.registerMember(1001L),
                "event-1",
                "REALTIME_CONNECTED",
                Map.of("memberId", 1001L)
        )).isTrue();
    }

    @Test
    @DisplayName("완료된 emitter 전송은 실패하고 registry에서 제거된다")
    void removesCompletedEmitterAfterDirectSendFailure() {
        SseEmitter emitter = registry.registerMember(1001L);
        emitter.complete();

        boolean sent = registry.sendToEmitter(
                emitter,
                "event-1",
                "REALTIME_CONNECTED",
                Map.of("memberId", 1001L)
        );

        assertThat(sent).isFalse();
        assertThat(registry.countMemberConnections(1001L)).isZero();
    }

    @Test
    @DisplayName("회원과 그룹 event 전송 실패 시 각 registry에서 emitter를 제거한다")
    void removesMemberAndGroupEmittersAfterSendFailure() {
        SseEmitter memberEmitter = registry.registerMember(1001L);
        SseEmitter groupEmitter = registry.registerGroup(3001L, 1001L);
        memberEmitter.complete();
        groupEmitter.complete();

        registry.sendToMember(1001L, "event-1", "TEST_EVENT", Map.of());
        registry.sendToGroup(3001L, "event-2", "TEST_EVENT", Map.of());

        assertThat(registry.countMemberConnections(1001L)).isZero();
        assertThat(registry.countGroupConnections(3001L)).isZero();
    }

    @Test
    @DisplayName("disconnect 전송 실패 후 completeWithError를 다시 호출하지 않는다")
    void doesNotCompleteWithErrorAfterDisconnectedClientFailure() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        AsyncRequestNotUsableException exception =
                new AsyncRequestNotUsableException("ServletOutputStream failed to write");
        doThrow(exception).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        boolean sent = registry.sendToEmitter(
                emitter,
                "event-1",
                "TEST_EVENT",
                Map.of()
        );

        assertThat(sent).isFalse();
        verify(emitter, never()).completeWithError(any());
    }

    @Test
    @DisplayName("한 emitter의 IllegalStateException이 다른 연결 정리를 막지 않는다")
    void continuesAfterUnexpectedIllegalStateException() throws IOException {
        SseEmitter failedEmitter = mock(SseEmitter.class);
        SseEmitter healthyEmitter = mock(SseEmitter.class);
        doThrow(new IllegalStateException("Failed to serialize event"))
                .when(failedEmitter)
                .send(any(SseEmitter.SseEventBuilder.class));
        registry.registerMember(1001L, failedEmitter);
        registry.registerMember(1001L, healthyEmitter);

        registry.sendToMember(1001L, "event-1", "TEST_EVENT", Map.of());

        verify(healthyEmitter).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(registry.countMemberConnections(1001L)).isEqualTo(1);
    }
}
