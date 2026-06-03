package matchuri.backend.api.realtime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
                    - 그룹 초대와 그룹장 전용 알림을 수신합니다.
                    - 연결 직후 `REALTIME_CONNECTED` 이벤트를 보냅니다.
                    - 기본 연결 timeout은 30분이며, 서버는 30초마다 heartbeat comment를 보낼 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SSE 연결 성공",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            examples = @ExampleObject(
                                    name = "connected",
                                    value = """
                                            id: 0c0f3b5b-b08c-40d1-b5a1-fcf9939b57df
                                            event: REALTIME_CONNECTED
                                            data: {"eventType":"REALTIME_CONNECTED","groupId":null}

                                            """
                            )
                    )
            )
    })
    SseEmitter connectMemberStream();

    @Operation(
            summary = "그룹 실시간 이벤트 스트림",
            description = """
                    특정 그룹 상세/추천 화면에서 필요한 SSE 이벤트 스트림을 엽니다.

                    - `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 연결할 수 있습니다.
                    - 응답은 `text/event-stream`입니다.
                    - 추천 시작, 준비 상태, 후보 생성, 투표 진행률, 최종 확정 이벤트를 수신합니다.
                    - 연결 직후 `REALTIME_CONNECTED` 이벤트를 보냅니다.
                    - 기본 연결 timeout은 30분이며, 서버는 30초마다 heartbeat comment를 보낼 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "SSE 연결 성공",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            examples = @ExampleObject(
                                    name = "connected",
                                    value = """
                                            id: 0c0f3b5b-b08c-40d1-b5a1-fcf9939b57df
                                            event: REALTIME_CONNECTED
                                            data: {"eventType":"REALTIME_CONNECTED","groupId":3001}

                                            """
                            )
                    )
            )
    })
    SseEmitter connectGroupStream(Long groupId);
}
