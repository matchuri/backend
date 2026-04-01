package matchuri.backend.domain.member.service;

import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.api.member.dto.CreateMemberRequest;
import matchuri.backend.api.member.dto.CreateMemberResponse;
import matchuri.backend.api.member.dto.MemberProfileResponse;
import matchuri.backend.api.member.dto.UpdateMemberBasicInfoRequest;
import matchuri.backend.api.member.dto.UpdateMemberResponse;
import matchuri.backend.api.member.dto.UpdateMemberTasteProfileRequest;
import matchuri.backend.api.member.dto.WithdrawMemberResponse;

public interface MemberService {

    boolean existsByLoginId(String loginId);

    CreateMemberResponse createMember(CreateMemberRequest request);

    LoginResponse login(LoginRequest request);

    LogoutResponse logout();

    MemberProfileResponse getMyProfile();

    UpdateMemberResponse updateMyProfile(UpdateMemberBasicInfoRequest request);

    UpdateMemberResponse updateMyTasteProfile(UpdateMemberTasteProfileRequest request);

    WithdrawMemberResponse withdraw();
}
