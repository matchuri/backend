package matchuri.backend.api.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.member.dto.CreateMemberRequest;
import matchuri.backend.api.member.dto.CreateMemberResponse;
import matchuri.backend.api.member.dto.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.MemberProfileResponse;
import matchuri.backend.api.member.dto.UpdateMemberBasicInfoRequest;
import matchuri.backend.api.member.dto.UpdateMemberResponse;
import matchuri.backend.api.member.dto.UpdateMemberTasteProfileRequest;
import matchuri.backend.api.member.dto.WithdrawMemberResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.service.MemberService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.exception.RequestValidationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Override
    @PostMapping
    public ApiResponse<CreateMemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        return ApiResponse.success(memberService.createMember(request));
    }

    @Override
    @GetMapping("/exists/{loginId}")
    public ApiResponse<LoginIdExistsResponse> checkLoginIdExists(
            @PathVariable String loginId
    ) {
        validateLoginId(loginId);
        boolean exists = memberService.existsByLoginId(loginId);
        return ApiResponse.success(memberMapper.toLoginIdExistsResponse(loginId, exists));
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> getMyProfile() {
        return ApiResponse.success(memberService.getMyProfile());
    }

    @Override
    @PatchMapping("/me")
    public ApiResponse<UpdateMemberResponse> updateMyProfile(@Valid @RequestBody UpdateMemberBasicInfoRequest request) {
        return ApiResponse.success(memberService.updateMyProfile(request));
    }

    @Override
    @PatchMapping("/me/taste-profile")
    public ApiResponse<UpdateMemberResponse> updateMyTasteProfile(@Valid @RequestBody UpdateMemberTasteProfileRequest request) {
        return ApiResponse.success(memberService.updateMyTasteProfile(request));
    }

    @Override
    @DeleteMapping("/me")
    public ApiResponse<WithdrawMemberResponse> withdraw() {
        return ApiResponse.success(memberService.withdraw());
    }

    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw RequestValidationException.invalidPathVariable("loginId", "로그인 아이디는 비어 있을 수 없습니다.");
        }

        if (loginId.length() > Member.LOGIN_ID_MAX_SIZE) {
            throw RequestValidationException.invalidPathVariable("loginId", "로그인 아이디는 50자를 초과할 수 없습니다.");
        }

        if (!loginId.matches(Member.LOGIN_ID_PATTERN)) {
            throw RequestValidationException.invalidPathVariable(
                    "loginId",
                    "로그인 아이디는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다."
            );
        }
    }
}
