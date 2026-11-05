package matchuri.backend.api.common;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.common.dto.response.HomeResponse;
import matchuri.backend.domain.common.service.CommonApplicationService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommonController implements CommonApi {

    private final CommonApplicationService commonApplicationService;

    @Override
    @GetMapping("/home")
    public ApiResponse<HomeResponse> home(@AuthenticatedMemberId Long memberId) {
        return ApiResponse.success(commonApplicationService.getHome(memberId));
    }
}
