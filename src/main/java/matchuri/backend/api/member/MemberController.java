package matchuri.backend.api.member;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.member.dto.request.CreateMemberRequest;
import matchuri.backend.api.member.dto.request.RegisterLocalMemberRequest;
import matchuri.backend.api.member.dto.request.PutMemberLocationRequest;
import matchuri.backend.api.member.dto.request.SetPresetProfileImageRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberBasicInfoRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberPasswordRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberTasteProfileRequest;
import matchuri.backend.api.member.dto.response.CreateMemberResponse;
import matchuri.backend.api.member.dto.response.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.response.MemberLocationResponse;
import matchuri.backend.api.member.dto.response.MemberProfileImageResponse;
import matchuri.backend.api.member.dto.response.MemberProfileResponse;
import matchuri.backend.api.member.dto.response.MemberPresetProfileImageResponse;
import matchuri.backend.api.member.dto.response.MemberTasteProfileSummaryResponse;
import matchuri.backend.api.member.dto.response.MemberTasteProfileUpdateResponse;
import matchuri.backend.api.member.dto.response.NicknameExistsResponse;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.api.member.dto.response.UpdateMemberPasswordResponse;
import matchuri.backend.api.member.dto.response.UpdateMemberResponse;
import matchuri.backend.api.member.dto.response.WithdrawMemberResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.member.result.MemberPresetProfileImageResult;
import matchuri.backend.domain.member.service.MemberService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Deprecated
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

    @Deprecated
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
        boolean exists = memberService.existsByLoginId(loginId);
        LoginIdExistsResponse response = memberMapper.toLoginIdExistsResponse(loginId, exists);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/exists/nickname/{nickname}")
    public ApiResponse<NicknameExistsResponse> checkNicknameExists(@PathVariable String nickname) {
        boolean exists = memberService.existsByNickname(nickname);
        NicknameExistsResponse response = memberMapper.toNicknameExistsResponse(nickname, exists);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> getMyProfile(@AuthenticatedMemberId Long memberId) {
        var myProfile = memberService.getMyProfile(memberId);
        var response = memberMapper.toMemberProfileResponse(myProfile);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/profile/preset-image")
    public ApiResponse<List<MemberPresetProfileImageResponse>> getPresetProfileImages(@AuthenticatedMemberId Long memberId) {
        List<MemberPresetProfileImageResult> results = memberService.getPresetProfileImages(memberId);
        List<MemberPresetProfileImageResponse> response = memberMapper.toMemberPresetProfileImageResponses(results);
        return ApiResponse.success(response);
    }

    @Override
    @PutMapping("/profile/preset-image")
    public ApiResponse<MemberProfileImageResponse> setPresetProfileImage(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody SetPresetProfileImageRequest request
    ) {
        var result = memberService.setPresetProfileImage(memberId, request.presetProfileImageId());
        return ApiResponse.success(memberMapper.toMemberProfileImageResponse(result));
    }

    @Override
    @GetMapping("/me/location")
    public ApiResponse<MemberLocationResponse> getMyLocation(@AuthenticatedMemberId Long memberId) {
        var result = memberService.getMyLocation(memberId);
        return ApiResponse.success(result == null ? null : memberMapper.toMemberLocationResponse(result));
    }

    @Override
    @PutMapping("/me/location")
    public ApiResponse<MemberLocationResponse> putMyLocation(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody PutMemberLocationRequest request
    ) {
        var command = memberMapper.toPutMemberLocationCommand(request);
        var result = memberService.putMyLocation(memberId, command);
        return ApiResponse.success(memberMapper.toMemberLocationResponse(result));
    }

    @Override
    @GetMapping("/me/taste-profile")
    public ApiResponse<MemberTasteProfileSummaryResponse> getMyTasteProfile(@AuthenticatedMemberId Long memberId) {
        var myTasteProfile = memberService.getMyTasteProfile(memberId);
        MemberTasteProfileSummaryResponse response = memberMapper.toMemberTasteProfileSummaryResponse(myTasteProfile);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/me")
    public ApiResponse<UpdateMemberResponse> updateMyProfile(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody UpdateMemberBasicInfoRequest request
    ) {
        var command = memberMapper.toUpdateMemberBasicInfoCommand(request.nickname());
        var result = memberService.updateMyProfile(memberId, command);
        UpdateMemberResponse response = memberMapper.toUpdateMemberResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/me/password")
    public ApiResponse<UpdateMemberPasswordResponse> updateMyPassword(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody UpdateMemberPasswordRequest request
    ) {
        var command = memberMapper.toUpdateMemberPasswordCommand(request);
        var result = memberService.updateMyPassword(memberId, command);
        UpdateMemberPasswordResponse response = memberMapper.toUpdateMemberPasswordResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/me/taste-profile")
    public ApiResponse<MemberTasteProfileUpdateResponse> updateMyTasteProfile(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody UpdateMemberTasteProfileRequest request) {
        var command = memberMapper.toUpdateMemberTasteProfileCommand(request);
        var result = memberService.updateMyTasteProfile(memberId, command);
        var response = memberMapper.toMemberTasteProfileUpdateResponse(result);
        return ApiResponse.success(response);
    }

    @Override
    @DeleteMapping("/me")
    public ApiResponse<WithdrawMemberResponse> withdraw(@AuthenticatedMemberId Long memberId) {
        var withdraw = memberService.withdraw(memberId);
        WithdrawMemberResponse response = memberMapper.toWithdrawMemberResponse(withdraw);

        return ApiResponse.success(response);
    }
}
