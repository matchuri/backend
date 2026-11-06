package matchuri.backend.domain.realtime.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;
import matchuri.backend.domain.realtime.result.RealtimeConnectedPayload;
import matchuri.backend.domain.realtime.result.RealtimeEventEnvelope;
import matchuri.backend.domain.realtime.support.RealtimeSseEmitterRegistry;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class RealtimeEventService {

    private final MemberReader memberReader;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final MemberRepository memberRepository;
    private final RealtimeSseEmitterRegistry emitterRegistry;

    @Transactional(readOnly = true)
    public SseEmitter connectMemberStream(Long memberId) {
        Member member = memberReader.getActiveMember(memberId);
        SseEmitter emitter = emitterRegistry.registerMember(member.getId());

        sendConnected(emitter, member.getId(), null);

        return emitter;
    }

    @Transactional(readOnly = true)
    public SseEmitter connectGroupStream(Long memberId, Long groupId) {
        Member member = memberReader.getActiveMember(memberId);

        if (groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(groupId, member.getId())
                .filter(membership -> membership.getMember().getStatus() == MemberStatus.ACTIVE).isEmpty()) {
            throw new BusinessException(GroupErrorCode.ACCESS_DENIED, groupId);
        }

        SseEmitter emitter = emitterRegistry.registerGroup(groupId, member.getId());

        sendConnected(emitter, member.getId(), groupId);

        return emitter;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void sendToMember(
            Long memberId,
            RealtimeEventType eventType,
            Long groupId,
            Long sessionId,
            Long actorMemberId,
            Object payload
    ) {
        if (memberRepository.findById(memberId)
                .filter(member -> member.getStatus() == MemberStatus.ACTIVE).isEmpty()) {
            emitterRegistry.completeMemberConnections(memberId);
            return;
        }
        RealtimeEventEnvelope envelope = envelope(eventType, groupId, sessionId, actorMemberId, payload);
        emitterRegistry.sendToMember(memberId, envelope.eventId(), eventType.name(), envelope);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void sendToGroup(
            Long groupId,
            RealtimeEventType eventType,
            Long eventGroupId,
            Long sessionId,
            Long actorMemberId,
            Object payload
    ) {
        sendToGroupMembers(groupId, activeMemberIds(groupId), eventType, eventGroupId,
                sessionId, actorMemberId, payload);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void sendToGroupMembers(
            Long groupId,
            Iterable<Long> memberIds,
            RealtimeEventType eventType,
            Long eventGroupId,
            Long sessionId,
            Long actorMemberId,
            Object payload
    ) {
        java.util.List<Long> eligible = activeMemberIds(groupId);
        emitterRegistry.retainGroupConnections(groupId, eligible);
        java.util.List<Long> targets = toList(memberIds).stream().filter(eligible::contains).toList();
        RealtimeEventEnvelope envelope = envelope(eventType, eventGroupId, sessionId, actorMemberId, payload);
        emitterRegistry.sendToGroupMembers(
                groupId,
                targets,
                envelope.eventId(),
                eventType.name(),
                envelope
        );
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void sendGroupDeletedToMembers(
            Long groupId, Iterable<Long> snapshotMemberIds, RealtimeEventType eventType,
            Long eventGroupId, Long sessionId, Long actorMemberId, Object payload
    ) {
        try {
            java.util.List<Long> eligible = memberRepository.findAllById(toList(snapshotMemberIds)).stream()
                    .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
                    .map(Member::getId).toList();
            RealtimeEventEnvelope envelope = envelope(eventType, eventGroupId, sessionId, actorMemberId, payload);
            emitterRegistry.sendToGroupMembers(groupId, eligible, envelope.eventId(), eventType.name(), envelope);
        } finally {
            emitterRegistry.completeGroupConnections(groupId);
        }
    }

    private java.util.List<Long> activeMemberIds(Long groupId) {
        if (groupRoomRepository.findById(groupId)
                .filter(room -> room.getStatus() != GroupRoomStatus.DELETED).isEmpty()) {
            return java.util.List.of();
        }
        return groupRoomMemberRepository.findActiveMembersByRoomId(groupId).stream()
                .map(membership -> membership.getMember().getId()).toList();
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
