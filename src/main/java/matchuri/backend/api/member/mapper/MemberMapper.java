package matchuri.backend.api.member.mapper;

import java.util.List;
import matchuri.backend.api.auth.dto.response.LoginResponse;
import matchuri.backend.api.auth.dto.response.LogoutResponse;
import matchuri.backend.api.member.dto.request.RegisterLocalMemberRequest;
import matchuri.backend.api.member.dto.response.CreateMemberResponse;
import matchuri.backend.api.member.dto.response.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.response.MemberProfileResponse;
import matchuri.backend.api.member.dto.response.MemberTasteProfileSummaryResponse;
import matchuri.backend.api.member.dto.response.NicknameExistsResponse;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.api.member.dto.response.UpdateMemberResponse;
import matchuri.backend.api.member.dto.response.WithdrawMemberResponse;
import matchuri.backend.domain.auth.service.LoginCommand;
import matchuri.backend.domain.auth.service.LoginPayload;
import matchuri.backend.domain.auth.service.LogoutResult;
import matchuri.backend.domain.auth.service.OAuth2ExchangeCommand;
import matchuri.backend.domain.member.service.CreateMemberCommand;
import matchuri.backend.domain.member.service.CreateMemberResult;
import matchuri.backend.domain.member.service.MemberProfileResult;
import matchuri.backend.domain.member.service.RegisterLocalMemberCommand;
import matchuri.backend.domain.member.service.RegisterLocalMemberResult;
import matchuri.backend.domain.member.service.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.service.UpdateMemberBasicInfoCommand;
import matchuri.backend.domain.member.service.UpdateMemberResult;
import matchuri.backend.domain.member.service.UpdateMemberTasteProfileCommand;
import matchuri.backend.domain.member.service.WithdrawMemberResult;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public LoginIdExistsResponse toLoginIdExistsResponse(String loginId, boolean exists) {
        return new LoginIdExistsResponse(loginId, exists);
    }

    public NicknameExistsResponse toNicknameExistsResponse(String nickname, boolean exists) {
        return new NicknameExistsResponse(nickname, exists);
    }

    public CreateMemberCommand toCreateMemberCommand(String loginId, String password) {
        return new CreateMemberCommand(loginId, password);
    }

    public CreateMemberResponse toCreateMemberResponse(CreateMemberResult result) {
        return new CreateMemberResponse(result.memberId(), result.loginId(), result.createdAt());
    }

    public RegisterLocalMemberCommand toRegisterLocalMemberCommand(RegisterLocalMemberRequest request) {
        return new RegisterLocalMemberCommand(
                request.loginId(),
                request.password(),
                request.nickname(),
                request.agreements().stream()
                        .map(agreement -> new SubmitRequiredAgreementsCommand.AgreementConsentCommand(
                                agreement.agreementType(),
                                agreement.agreementVersion()
                        ))
                        .toList()
        );
    }

    public RegisterLocalMemberResponse toRegisterLocalMemberResponse(RegisterLocalMemberResult result) {
        return new RegisterLocalMemberResponse(
                result.memberId(),
                result.loginId(),
                result.nickname(),
                result.createdAt()
        );
    }

    public LoginCommand toLoginCommand(String loginId, String password) {
        return new LoginCommand(loginId, password);
    }

    public OAuth2ExchangeCommand toOAuth2ExchangeCommand(matchuri.backend.domain.member.entity.SocialProviderType provider, String code) {
        return new OAuth2ExchangeCommand(provider, code);
    }

    public LoginResponse toLoginResponse(LoginPayload payload) {
        return new LoginResponse(
                payload.accessToken(),
                null,
                payload.expiresIn(),
                new LoginResponse.LoginMemberSummary(payload.memberId(), payload.role())
        );
    }

    public LogoutResponse toLogoutResponse(LogoutResult result) {
        return new LogoutResponse(result.loggedOut());
    }

    public CreateMemberResponse toCreateMemberResponse(Member member) {
        return new CreateMemberResponse(member.getId(), member.getLoginId(), member.getCreatedAt());
    }

    public LoginResponse toLoginResponse(Member member, String accessToken, long expiresIn, String refreshToken) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                expiresIn,
                new LoginResponse.LoginMemberSummary(member.getId(), member.getMemberRole().name())
        );
    }

    public MemberProfileResponse toMemberProfileResponse(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getNickname()
        );
    }

    public MemberProfileResponse toMemberProfileResponse(MemberProfileResult result) {
        return new MemberProfileResponse(result.id(), result.nickname());
    }

    public UpdateMemberBasicInfoCommand toUpdateMemberBasicInfoCommand(String nickname) {
        return new UpdateMemberBasicInfoCommand(nickname);
    }

    public UpdateMemberTasteProfileCommand toUpdateMemberTasteProfileCommand(String profileVersion) {
        return new UpdateMemberTasteProfileCommand(profileVersion);
    }

    public UpdateMemberResponse toUpdateMemberResponse(Member member) {
        return new UpdateMemberResponse(member.getId(), member.getUpdatedAt());
    }

    public UpdateMemberResponse toUpdateMemberResponse(UpdateMemberResult result) {
        return new UpdateMemberResponse(result.id(), result.updatedAt());
    }

    public WithdrawMemberResponse toWithdrawMemberResponse(Member member) {
        return new WithdrawMemberResponse(member.getId(), member.getStatus().name());
    }

    public WithdrawMemberResponse toWithdrawMemberResponse(WithdrawMemberResult result) {
        return new WithdrawMemberResponse(result.id(), result.status());
    }

    private MemberTasteProfileSummaryResponse toTasteProfileSummary(MemberTasteProfile tasteProfile) {
        if (tasteProfile == null) {
            return null;
        }

        return new MemberTasteProfileSummaryResponse(List.of(), List.of(), tasteProfile.getProfileVersion());
    }
}
