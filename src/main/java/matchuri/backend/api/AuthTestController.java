package matchuri.backend.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.AuthTestApi;
import matchuri.backend.api.auth.dto.request.LoginTestRequest;
import matchuri.backend.api.auth.dto.response.LoginResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.auth.service.LocalTestAuthService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local & !dev & !prod")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class AuthTestController implements AuthTestApi {

    private final LocalTestAuthService localTestAuthService;
    private final MemberMapper memberMapper;

    @Override
    @PostMapping("/login")
    public ApiResponse<LoginResponse> loginTest(@Valid @RequestBody LoginTestRequest request) {
        var payload = localTestAuthService.login(request.memberId());
        return ApiResponse.success(memberMapper.toLoginResponse(payload));
    }
}
