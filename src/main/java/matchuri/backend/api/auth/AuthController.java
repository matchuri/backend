package matchuri.backend.api.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.domain.member.service.MemberService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

    private final MemberService memberService;

    @Override
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(memberService.login(request));
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout() {
        return ApiResponse.success(memberService.logout());
    }
}
