package matchuri.backend.api.realtime;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.realtime.service.RealtimeEventService;
import matchuri.backend.global.security.AuthenticatedMemberId;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RealtimeController implements RealtimeApi {

    private final RealtimeEventService realtimeEventService;

    @Override
    @GetMapping(path = "/realtime/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectMemberStream(@AuthenticatedMemberId Long memberId) {
        return realtimeEventService.connectMemberStream(memberId);
    }

    @Override
    @GetMapping(path = "/groups/{groupId}/realtime/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectGroupStream(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        return realtimeEventService.connectGroupStream(memberId, groupId);
    }
}
