package matchuri.backend.api.member;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.member.dto.LoginIdExistsResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.global.exception.RequestValidationException;
import matchuri.backend.domain.member.service.MemberService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @GetMapping("/exists/{loginId}")
    public ApiResponse<LoginIdExistsResponse> checkLoginIdExists(
            @PathVariable String loginId
    ) {
        validateLoginId(loginId);
        boolean exists = memberService.existsByLoginId(loginId);
        return ApiResponse.success(memberMapper.toLoginIdExistsResponse(loginId, exists));
    }

    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw RequestValidationException.invalidPathVariable("loginId", "loginId는 비어 있을 수 없습니다.");
        }

        if (loginId.length() > 50) {
            throw RequestValidationException.invalidPathVariable("loginId", "loginId는 50자를 초과할 수 없습니다.");
        }

        if (!loginId.matches("^[A-Za-z0-9._-]+$")) {
            throw RequestValidationException.invalidPathVariable(
                    "loginId",
                    "loginId는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다."
            );
        }
    }
}
