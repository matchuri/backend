package matchuri.backend.domain.realtime.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
