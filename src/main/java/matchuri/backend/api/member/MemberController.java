package matchuri.backend.api.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.member.dto.request.CreateMemberRequest;
import matchuri.backend.api.member.dto.request.RegisterLocalMemberRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberBasicInfoRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberPasswordRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberTasteProfileRequest;
import matchuri.backend.api.member.dto.response.CreateMemberResponse;
import matchuri.backend.api.member.dto.response.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.response.MemberProfileResponse;
import matchuri.backend.api.member.dto.response.MemberTasteProfileSummaryResponse;
import matchuri.backend.api.member.dto.response.MemberTasteProfileUpdateResponse;
import matchuri.backend.api.member.dto.response.NicknameExistsResponse;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.api.member.dto.response.UpdateMemberPasswordResponse;
import matchuri.backend.api.member.dto.response.UpdateMemberResponse;
import matchuri.backend.api.member.dto.response.WithdrawMemberResponse;
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
    @PostMapping("/signup")
    public ApiResponse<RegisterLocalMemberResponse> registerLocalMember(
            @Valid @RequestBody RegisterLocalMemberRequest request
    ) {
        var command = memberMapper.toRegisterLocalMemberCommand(request);
        var result = memberService.registerLocalMember(command);
        RegisterLocalMemberResponse response = memberMapper.toRegisterLocalMemberResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping
    public ApiResponse<CreateMemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        var command = memberMapper.toCreateMemberCommand(request.loginId(), request.password());
        var result = memberService.createMember(command);
        CreateMemberResponse response = memberMapper.toCreateMemberResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/exists/{loginId}")
    public ApiResponse<LoginIdExistsResponse> checkLoginIdExists(
            @PathVariable String loginId
    ) {
        validateLoginId(loginId);
        boolean exists = memberService.existsByLoginId(loginId);
        LoginIdExistsResponse response = memberMapper.toLoginIdExistsResponse(loginId, exists);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/exists/nickname/{nickname}")
    public ApiResponse<NicknameExistsResponse> checkNicknameExists(@PathVariable String nickname) {
        validateNickname(nickname);
        boolean exists = memberService.existsByNickname(nickname);
        NicknameExistsResponse response = memberMapper.toNicknameExistsResponse(nickname, exists);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> getMyProfile() {
        var myProfile = memberService.getMyProfile();
        MemberProfileResponse response = memberMapper.toMemberProfileResponse(myProfile);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/me/taste-profile")
    public ApiResponse<MemberTasteProfileSummaryResponse> getMyTasteProfile() {
        var myTasteProfile = memberService.getMyTasteProfile();
        MemberTasteProfileSummaryResponse response = memberMapper.toMemberTasteProfileSummaryResponse(myTasteProfile);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/me")
    public ApiResponse<UpdateMemberResponse> updateMyProfile(@Valid @RequestBody UpdateMemberBasicInfoRequest request) {
        var command = memberMapper.toUpdateMemberBasicInfoCommand(request.nickname());
        var result = memberService.updateMyProfile(command);
        UpdateMemberResponse response = memberMapper.toUpdateMemberResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/me/password")
    public ApiResponse<UpdateMemberPasswordResponse> updateMyPassword(
            @Valid @RequestBody UpdateMemberPasswordRequest request
    ) {
        var command = memberMapper.toUpdateMemberPasswordCommand(request);
        var result = memberService.updateMyPassword(command);
        UpdateMemberPasswordResponse response = memberMapper.toUpdateMemberPasswordResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/me/taste-profile")
    public ApiResponse<MemberTasteProfileUpdateResponse> updateMyTasteProfile(
            @Valid @RequestBody UpdateMemberTasteProfileRequest request) {
        var command = memberMapper.toUpdateMemberTasteProfileCommand(request);
        var result = memberService.updateMyTasteProfile(command);
        var response = memberMapper.toMemberTasteProfileUpdateResponse(result);
        return ApiResponse.success(response);
    }

    @Override
    @DeleteMapping("/me")
    public ApiResponse<WithdrawMemberResponse> withdraw() {
        var withdraw = memberService.withdraw();
        WithdrawMemberResponse response = memberMapper.toWithdrawMemberResponse(withdraw);

        return ApiResponse.success(response);
    }

    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw RequestValidationException.invalidPathVariable("loginId", "로그인 아이디는 비어 있을 수 없습니다.");
        }

        if (loginId.length() > Member.LOGIN_ID_MAX_SIZE) {
            throw RequestValidationException.invalidPathVariable(
                    "loginId",
                    "로그인 아이디는 " + Member.LOGIN_ID_MAX_SIZE + "자를 초과할 수 없습니다."
            );
        }

        if (!loginId.matches(Member.LOGIN_ID_PATTERN)) {
            throw RequestValidationException.invalidPathVariable(
                    "loginId",
                    "로그인 아이디는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다."
            );
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw RequestValidationException.invalidPathVariable("nickname", "닉네임은 비어 있을 수 없습니다.");
        }

        if (nickname.length() > Member.NICKNAME_MAX_SIZE) {
            throw RequestValidationException.invalidPathVariable(
                    "nickname",
                    "닉네임은 " + Member.NICKNAME_MAX_SIZE + "자를 초과할 수 없습니다."
            );
        }
    }
}
