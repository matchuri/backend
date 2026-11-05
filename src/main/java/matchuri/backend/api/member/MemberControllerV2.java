package matchuri.backend.api.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.member.dto.request.RegisterLocalMemberV2Request;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.member.service.MemberService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/members")
public class MemberControllerV2 implements MemberApiV2 {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Override
    @PostMapping("/signup")
    public ApiResponse<RegisterLocalMemberResponse> registerLocalMember(
            @Valid @RequestBody RegisterLocalMemberV2Request request
    ) {
        var command = memberMapper.toRegisterLocalMemberV2Command(request);
        var result = memberService.registerLocalMemberV2(command);
        RegisterLocalMemberResponse response = memberMapper.toRegisterLocalMemberResponse(result);

        return ApiResponse.success(response);
    }
}
