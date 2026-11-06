package matchuri.backend.api.realtime;

import matchuri.backend.global.security.AuthenticatedMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Realtime", description = "실시간 이벤트 API")
public interface RealtimeApi {

    @Operation(
            summary = "내 실시간 이벤트 스트림",
            description = """
                    로그인 회원 개인에게 도착하는 SSE 이벤트 스트림을 엽니다.

                    - `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
                    - 응답은 `text/event-stream`입니다.
                    - 그룹 초대와 그룹장 전용 투표 완료 알림을 수신합니다.
                    - 연결 직후 `REALTIME_CONNECTED` 이벤트를 보냅니다.
                    - 기본 연결 timeout은 30분이며, 서버는 30초마다 heartbeat comment를 보낼 수 있습니다.

                    수신 가능한 eventType:
                    - `REALTIME_CONNECTED`
                    - `GROUP_INVITE_CREATED`
                    - `GROUP_RECOMMENDATION_VOTE_COMPLETED`

                    SSE frame의 `event` 값과 `data.eventType` 값은 동일합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SSE 연결 성공",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    description = """
                                            SSE event stream입니다. 각 frame은 `id`, `event`, `data` line으로 구성됩니다.
                                            `event` line에는 RealtimeEventType 값이 들어가며, `data` line에는 JSON envelope가 들어갑니다.
                                            heartbeat는 `: heartbeat` comment frame으로 전송될 수 있습니다.
                                            """
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "connected",
                                            summary = "연결 확인 이벤트",
                                            value = """
                                                    id: 0c0f3b5b-b08c-40d1-b5a1-fcf9939b57df
                                                    event: REALTIME_CONNECTED
                                                    data: {"eventId":"0c0f3b5b-b08c-40d1-b5a1-fcf9939b57df","eventType":"REALTIME_CONNECTED","occurredAt":"2026-06-03T10:00:00","groupId":null,"sessionId":null,"actorMemberId":1001,"payload":{"memberId":1001,"groupId":null,"connectedAt":"2026-06-03T10:00:00"}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "inviteCreated",
                                            summary = "그룹 초대 생성 이벤트",
                                            value = """
                                                    id: 7a9c5d82-9a83-4d22-8724-6e1b1f88c001
                                                    event: GROUP_INVITE_CREATED
                                                    data: {"eventId":"7a9c5d82-9a83-4d22-8724-6e1b1f88c001","eventType":"GROUP_INVITE_CREATED","occurredAt":"2026-06-03T10:05:00","groupId":3001,"sessionId":null,"actorMemberId":1001,"payload":{"inviteId":9001,"groupId":3001,"groupName":"점심팟","requestMemberId":1001,"requestMemberNickname":"그룹장","expiresAt":"2026-06-04T10:05:00"}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "voteCompleted",
                                            summary = "전원 투표 완료 이벤트",
                                            value = """
                                                    id: a0ce0f29-1a8d-4432-a2a2-97aa9d66c001
                                                    event: GROUP_RECOMMENDATION_VOTE_COMPLETED
                                                    data: {"eventId":"a0ce0f29-1a8d-4432-a2a2-97aa9d66c001","eventType":"GROUP_RECOMMENDATION_VOTE_COMPLETED","occurredAt":"2026-06-03T12:30:00","groupId":3001,"sessionId":5001,"actorMemberId":null,"payload":{"sessionId":5001,"voteProgress":{"totalMemberCount":3,"votedMemberCount":3,"allVoted":true},"finalizeRequired":true}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "heartbeat",
                                            summary = "heartbeat comment",
                                            value = """
                                                    : heartbeat

                                                    """
                                    )
                            }
                    )
            )
    })
    SseEmitter connectMemberStream(@AuthenticatedMemberId Long memberId);

    @Operation(
            summary = "그룹 실시간 이벤트 스트림",
            description = """
                    특정 그룹 상세/추천 화면에서 필요한 SSE 이벤트 스트림을 엽니다.

                    - `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 연결할 수 있습니다.
                    - 응답은 `text/event-stream`입니다.
                    - 멤버 참여/탈퇴, 그룹 삭제, 추천 시작, 준비 상태, 후보 생성, 투표 진행률, 최종 확정 이벤트를 수신합니다.
                    - 연결 직후 `REALTIME_CONNECTED` 이벤트를 보냅니다.
                    - 기본 연결 timeout은 30분이며, 서버는 30초마다 heartbeat comment를 보낼 수 있습니다.

                    수신 가능한 eventType:
                    - `REALTIME_CONNECTED`
                    - `GROUP_MEMBER_JOINED`
                    - `GROUP_MEMBER_LEFT`
                    - `GROUP_DELETED`
                    - `GROUP_RECOMMENDATION_STARTED`
                    - `GROUP_RECOMMENDATION_READINESS_UPDATED`
                    - `GROUP_RECOMMENDATION_OPENED`
                    - `GROUP_RECOMMENDATION_VOTE_UPDATED`
                    - `GROUP_RECOMMENDATION_FINALIZED`

                    SSE frame의 `event` 값과 `data.eventType` 값은 동일합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SSE 연결 성공",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    description = """
                                            SSE event stream입니다. 각 frame은 `id`, `event`, `data` line으로 구성됩니다.
                                            `event` line에는 RealtimeEventType 값이 들어가며, `data` line에는 JSON envelope가 들어갑니다.
                                            heartbeat는 `: heartbeat` comment frame으로 전송될 수 있습니다.
                                            """
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "connected",
                                            summary = "연결 확인 이벤트",
                                            value = """
                                                    id: 0c0f3b5b-b08c-40d1-b5a1-fcf9939b57df
                                                    event: REALTIME_CONNECTED
                                                    data: {"eventId":"0c0f3b5b-b08c-40d1-b5a1-fcf9939b57df","eventType":"REALTIME_CONNECTED","occurredAt":"2026-06-03T10:00:00","groupId":3001,"sessionId":null,"actorMemberId":1001,"payload":{"memberId":1001,"groupId":3001,"connectedAt":"2026-06-03T10:00:00"}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "memberJoined",
                                            summary = "멤버 참여 이벤트",
                                            value = """
                                                    id: 8f31037b-ae70-4e62-b22f-0efac87bc001
                                                    event: GROUP_MEMBER_JOINED
                                                    data: {"eventId":"8f31037b-ae70-4e62-b22f-0efac87bc001","eventType":"GROUP_MEMBER_JOINED","occurredAt":"2026-06-03T10:10:00","groupId":3001,"sessionId":null,"actorMemberId":1002,"payload":{"groupId":3001,"memberId":1002,"memberNickname":"새멤버","joinedAt":"2026-06-03T10:10:00"}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "memberLeft",
                                            summary = "멤버 탈퇴 이벤트",
                                            value = """
                                                    id: c9df7e4d-9e16-465a-bc74-21b0e6abc001
                                                    event: GROUP_MEMBER_LEFT
                                                    data: {"eventId":"c9df7e4d-9e16-465a-bc74-21b0e6abc001","eventType":"GROUP_MEMBER_LEFT","occurredAt":"2026-06-03T10:20:00","groupId":3001,"sessionId":null,"actorMemberId":1002,"payload":{"groupId":3001,"memberId":1002,"memberNickname":"기존멤버","leftAt":"2026-06-03T10:20:00"}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "groupDeleted",
                                            summary = "그룹 삭제 이벤트",
                                            value = """
                                                    id: c1848a83-9d3d-43d5-82da-64e9c4abc001
                                                    event: GROUP_DELETED
                                                    data: {"eventId":"c1848a83-9d3d-43d5-82da-64e9c4abc001","eventType":"GROUP_DELETED","occurredAt":"2026-06-03T10:30:00","groupId":3001,"sessionId":null,"actorMemberId":1001,"payload":{"groupId":3001,"deletedByMemberId":1001,"deletedAt":"2026-06-03T10:30:00"}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "recommendationStarted",
                                            summary = "그룹 추천 시작 이벤트",
                                            value = """
                                                    id: 06a66d75-7769-45bc-a85f-7ff197abc001
                                                    event: GROUP_RECOMMENDATION_STARTED
                                                    data: {"eventId":"06a66d75-7769-45bc-a85f-7ff197abc001","eventType":"GROUP_RECOMMENDATION_STARTED","occurredAt":"2026-06-03T12:00:00","groupId":3001,"sessionId":5001,"actorMemberId":1001,"payload":{"sessionId":5001,"status":"PREPARING","readinessProgress":{"totalMemberCount":3,"readyMemberCount":0,"allReady":false}}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "readinessUpdated",
                                            summary = "준비 상태 갱신 이벤트",
                                            value = """
                                                    id: 3b31a812-5ed1-441e-a0c8-3af0aeabc001
                                                    event: GROUP_RECOMMENDATION_READINESS_UPDATED
                                                    data: {"eventId":"3b31a812-5ed1-441e-a0c8-3af0aeabc001","eventType":"GROUP_RECOMMENDATION_READINESS_UPDATED","occurredAt":"2026-06-03T12:05:00","groupId":3001,"sessionId":5001,"actorMemberId":1002,"payload":{"sessionId":5001,"status":"PREPARING","readyMemberId":1002,"readyMemberNickname":"멤버","readinessProgress":{"totalMemberCount":3,"readyMemberCount":1,"allReady":false}}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "recommendationOpened",
                                            summary = "후보 생성 이벤트",
                                            value = """
                                                    id: 0bc3e221-65fd-4d73-b6a6-10f216abc001
                                                    event: GROUP_RECOMMENDATION_OPENED
                                                    data: {"eventId":"0bc3e221-65fd-4d73-b6a6-10f216abc001","eventType":"GROUP_RECOMMENDATION_OPENED","occurredAt":"2026-06-03T12:10:00","groupId":3001,"sessionId":5001,"actorMemberId":null,"payload":{"sessionId":5001,"status":"OPEN","candidates":[{"candidateId":8001,"menuItemId":1001,"menuName":"비빔밥","reason":"그룹 취향과 잘 맞습니다."},{"candidateId":8002,"menuItemId":1002,"menuName":"쌀국수","reason":"가벼운 메뉴 선호를 반영했습니다."},{"candidateId":8003,"menuItemId":1003,"menuName":"돈까스","reason":"선호도가 높은 메뉴입니다."}],"voteProgress":{"totalMemberCount":3,"votedMemberCount":0,"allVoted":false}}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "voteUpdated",
                                            summary = "투표 진행률 갱신 이벤트",
                                            value = """
                                                    id: 201512f1-7e1a-4c6d-811c-6b8183abc001
                                                    event: GROUP_RECOMMENDATION_VOTE_UPDATED
                                                    data: {"eventId":"201512f1-7e1a-4c6d-811c-6b8183abc001","eventType":"GROUP_RECOMMENDATION_VOTE_UPDATED","occurredAt":"2026-06-03T12:20:00","groupId":3001,"sessionId":5001,"actorMemberId":null,"payload":{"sessionId":5001,"voteProgress":{"totalMemberCount":3,"votedMemberCount":2,"allVoted":false}}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "finalized",
                                            summary = "최종 후보 확정 이벤트",
                                            value = """
                                                    id: 4a5793e5-8022-4f0c-857f-a1f967abc001
                                                    event: GROUP_RECOMMENDATION_FINALIZED
                                                    data: {"eventId":"4a5793e5-8022-4f0c-857f-a1f967abc001","eventType":"GROUP_RECOMMENDATION_FINALIZED","occurredAt":"2026-06-03T12:40:00","groupId":3001,"sessionId":5001,"actorMemberId":1001,"payload":{"sessionId":5001,"status":"FINALIZED","finalizedAt":"2026-06-03T12:40:00","finalCandidate":{"candidateId":8001,"menuItemId":1001,"menuName":"비빔밥","reason":"그룹 취향과 잘 맞습니다."}}}

                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "heartbeat",
                                            summary = "heartbeat comment",
                                            value = """
                                                    : heartbeat

                                                    """
                                    )
                            }
                    )
            )
    })
    SseEmitter connectGroupStream(@AuthenticatedMemberId Long memberId, Long groupId);
}
