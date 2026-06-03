package matchuri.backend.domain.realtime.support;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class RealtimeSseEmitterRegistry {

    private static final long SSE_TIMEOUT_MILLIS = 30L * 60L * 1000L;

    private final ConcurrentMap<Long, CopyOnWriteArrayList<EmitterConnection>> memberEmitters =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, CopyOnWriteArrayList<EmitterConnection>> groupEmitters =
            new ConcurrentHashMap<>();

    public SseEmitter registerMember(Long memberId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        EmitterConnection connection = new EmitterConnection(UUID.randomUUID().toString(), memberId, emitter);

        memberEmitters.computeIfAbsent(memberId, ignored -> new CopyOnWriteArrayList<>()).add(connection);
        registerCallbacks(emitter, () -> removeMemberEmitter(memberId, connection));

        return emitter;
    }

    public SseEmitter registerGroup(Long groupId, Long memberId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        EmitterConnection connection = new EmitterConnection(UUID.randomUUID().toString(), memberId, emitter);

        groupEmitters.computeIfAbsent(groupId, ignored -> new CopyOnWriteArrayList<>()).add(connection);
        registerCallbacks(emitter, () -> removeGroupEmitter(groupId, connection));

        return emitter;
    }

    public void sendToMember(Long memberId, String eventId, String eventName, Object data) {
        send(memberEmitters.getOrDefault(memberId, new CopyOnWriteArrayList<>()), eventId, eventName, data);
    }

    public void sendToGroup(Long groupId, String eventId, String eventName, Object data) {
        send(groupEmitters.getOrDefault(groupId, new CopyOnWriteArrayList<>()), eventId, eventName, data);
    }

    public void sendToGroupMembers(
            Long groupId,
            Collection<Long> memberIds,
            String eventId,
            String eventName,
            Object data
    ) {
        sendToMatchingMembers(
                groupEmitters.getOrDefault(groupId, new CopyOnWriteArrayList<>()),
                memberIds,
                eventId,
                eventName,
                data
        );
    }

    public boolean sendToEmitter(SseEmitter emitter, String eventId, String eventName, Object data) {
        try {
            sendEvent(emitter, eventId, eventName, data);
            return true;
        } catch (IOException | IllegalStateException exception) {
            log.debug("SSE direct send failed. eventName={}", eventName, exception);
            emitter.completeWithError(exception);
            return false;
        }
    }

    public int countMemberConnections(Long memberId) {
        List<EmitterConnection> connections = memberEmitters.get(memberId);
        return connections == null ? 0 : connections.size();
    }

    public int countGroupConnections(Long groupId) {
        List<EmitterConnection> connections = groupEmitters.get(groupId);
        return connections == null ? 0 : connections.size();
    }

    public int countTotalConnections() {
        return memberEmitters.values().stream().mapToInt(List::size).sum()
                + groupEmitters.values().stream().mapToInt(List::size).sum();
    }

    @Scheduled(fixedRateString = "PT30S")
    public void sendHeartbeat() {
        memberEmitters.values().forEach(this::sendHeartbeat);
        groupEmitters.values().forEach(this::sendHeartbeat);
    }

    private void registerCallbacks(SseEmitter emitter, Runnable cleanup) {
        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            cleanup.run();
            emitter.complete();
        });
        emitter.onError(exception -> cleanup.run());
    }

    private void send(List<EmitterConnection> connections, String eventId, String eventName, Object data) {
        connections.forEach(connection -> {
            try {
                sendEvent(connection.emitter(), eventId, eventName, data);
            } catch (IOException | IllegalStateException exception) {
                log.debug("SSE send failed. connectionId={}, eventName={}", connection.id(), eventName, exception);
                connection.emitter().completeWithError(exception);
                connections.remove(connection);
            }
        });
    }

    private void sendToMatchingMembers(
            List<EmitterConnection> connections,
            Collection<Long> memberIds,
            String eventId,
            String eventName,
            Object data
    ) {
        connections.forEach(connection -> {
            if (connection.memberId() == null || !memberIds.contains(connection.memberId())) {
                return;
            }

            try {
                sendEvent(connection.emitter(), eventId, eventName, data);
            } catch (IOException | IllegalStateException exception) {
                log.debug("SSE send failed. connectionId={}, eventName={}", connection.id(), eventName, exception);
                connection.emitter().completeWithError(exception);
                connections.remove(connection);
            }
        });
    }

    private void sendHeartbeat(List<EmitterConnection> connections) {
        connections.forEach(connection -> {
            try {
                connection.emitter().send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException | IllegalStateException exception) {
                log.debug("SSE heartbeat failed. connectionId={}", connection.id(), exception);
                connection.emitter().completeWithError(exception);
                connections.remove(connection);
            }
        });
    }

    private void sendEvent(SseEmitter emitter, String eventId, String eventName, Object data) throws IOException {
        emitter.send(SseEmitter.event()
                .id(eventId)
                .name(eventName)
                .data(data, MediaType.APPLICATION_JSON));
    }

    private void removeMemberEmitter(Long memberId, EmitterConnection connection) {
        removeEmitter(memberEmitters, memberId, connection);
    }

    private void removeGroupEmitter(Long groupId, EmitterConnection connection) {
        removeEmitter(groupEmitters, groupId, connection);
    }

    private void removeEmitter(
            ConcurrentMap<Long, CopyOnWriteArrayList<EmitterConnection>> emitters,
            Long key,
            EmitterConnection connection
    ) {
        CopyOnWriteArrayList<EmitterConnection> connections = emitters.get(key);
        if (connections == null) {
            return;
        }

        connections.remove(connection);
        if (connections.isEmpty()) {
            emitters.remove(key, connections);
        }
    }

    private record EmitterConnection(
            String id,
            Long memberId,
            SseEmitter emitter) {
    }
}
