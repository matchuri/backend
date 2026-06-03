package matchuri.backend.domain.realtime.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;
import matchuri.backend.domain.realtime.result.RealtimeConnectedPayload;
import matchuri.backend.domain.realtime.result.RealtimeEventEnvelope;
import matchuri.backend.domain.realtime.support.RealtimeSseEmitterRegistry;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class RealtimeEventService {

    private final ActiveMemberReader activeMemberReader;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final RealtimeSseEmitterRegistry emitterRegistry;

    @Transactional(readOnly = true)
    public SseEmitter connectMemberStream() {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        SseEmitter emitter = emitterRegistry.registerMember(member.getId());

        sendConnected(emitter, member.getId(), null);

        return emitter;
    }

    @Transactional(readOnly = true)
    public SseEmitter connectGroupStream(Long groupId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();

        if (!groupRoomMemberRepository.existsActiveMembershipInNotDeletedRoom(groupId, member.getId())) {
            throw new BusinessException(GroupErrorCode.ACCESS_DENIED, groupId);
        }

        SseEmitter emitter = emitterRegistry.registerGroup(groupId, member.getId());

        sendConnected(emitter, member.getId(), groupId);

        return emitter;
    }

    public void sendToMember(
            Long memberId,
            RealtimeEventType eventType,
            Long groupId,
            Long sessionId,
            Long actorMemberId,
            Object payload
    ) {
        RealtimeEventEnvelope envelope = envelope(eventType, groupId, sessionId, actorMemberId, payload);
        emitterRegistry.sendToMember(memberId, envelope.eventId(), eventType.name(), envelope);
    }

    public void sendToGroup(
            Long groupId,
            RealtimeEventType eventType,
            Long eventGroupId,
            Long sessionId,
            Long actorMemberId,
            Object payload
    ) {
        RealtimeEventEnvelope envelope = envelope(eventType, eventGroupId, sessionId, actorMemberId, payload);
        emitterRegistry.sendToGroup(groupId, envelope.eventId(), eventType.name(), envelope);
    }

    public void sendToGroupMembers(
            Long groupId,
            Iterable<Long> memberIds,
            RealtimeEventType eventType,
            Long eventGroupId,
            Long sessionId,
            Long actorMemberId,
            Object payload
    ) {
        RealtimeEventEnvelope envelope = envelope(eventType, eventGroupId, sessionId, actorMemberId, payload);
        emitterRegistry.sendToGroupMembers(
                groupId,
                toList(memberIds),
                envelope.eventId(),
                eventType.name(),
                envelope
        );
    }

    private java.util.List<Long> toList(Iterable<Long> memberIds) {
        java.util.ArrayList<Long> result = new java.util.ArrayList<>();
        memberIds.forEach(result::add);
        return result;
    }

    private void sendConnected(SseEmitter emitter, Long memberId, Long groupId) {
        RealtimeEventEnvelope envelope = envelope(
                RealtimeEventType.REALTIME_CONNECTED,
                groupId,
                null,
                memberId,
                new RealtimeConnectedPayload(memberId, groupId, LocalDateTime.now())
        );

        emitterRegistry.sendToEmitter(
                emitter,
                envelope.eventId(),
                RealtimeEventType.REALTIME_CONNECTED.name(),
                envelope
        );
    }

    private RealtimeEventEnvelope envelope(
            RealtimeEventType eventType,
            Long groupId,
            Long sessionId,
            Long actorMemberId,
            Object payload
    ) {
        return new RealtimeEventEnvelope(
                UUID.randomUUID().toString(),
                eventType,
                LocalDateTime.now(),
                groupId,
                sessionId,
                actorMemberId,
                payload
        );
    }
}
