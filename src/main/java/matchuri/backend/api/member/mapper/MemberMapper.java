package matchuri.backend.api.member.mapper;

import matchuri.backend.api.auth.dto.response.LoginResponse;
import matchuri.backend.api.auth.dto.response.LogoutResponse;
import matchuri.backend.api.member.dto.request.RegisterLocalMemberRequest;
import matchuri.backend.api.member.dto.response.CreateMemberResponse;
import matchuri.backend.api.member.dto.response.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.response.MemberTasteAttributeCategoryResponse;
import matchuri.backend.api.member.dto.response.MemberProfileResponse;
import matchuri.backend.api.member.dto.response.MemberTasteProfileSummaryResponse;
import matchuri.backend.api.member.dto.response.MemberTasteRestrictionIngredientResponse;
import matchuri.backend.api.member.dto.response.NicknameExistsResponse;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.api.member.dto.response.UpdateMemberResponse;
import matchuri.backend.api.member.dto.response.WithdrawMemberResponse;
import matchuri.backend.domain.auth.command.LoginCommand;
import matchuri.backend.domain.auth.command.OAuth2ExchangeCommand;
import matchuri.backend.domain.auth.result.LoginPayload;
import matchuri.backend.domain.auth.result.LogoutResult;
import matchuri.backend.domain.member.command.CreateMemberCommand;
import matchuri.backend.domain.member.command.RegisterLocalMemberCommand;
import matchuri.backend.domain.member.command.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.command.UpdateMemberBasicInfoCommand;
import matchuri.backend.domain.member.command.UpdateMemberTasteProfileCommand;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.result.CreateMemberResult;
import matchuri.backend.domain.member.result.MemberProfileResult;
import matchuri.backend.domain.member.result.MemberTasteProfileSummaryResult;
import matchuri.backend.domain.member.result.RegisterLocalMemberResult;
import matchuri.backend.domain.member.result.UpdateMemberResult;
import matchuri.backend.domain.member.result.WithdrawMemberResult;
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

    public OAuth2ExchangeCommand toOAuth2ExchangeCommand(SocialProviderType provider, String code) {
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

    public MemberProfileResponse toMemberProfileResponse(MemberProfileResult result) {
        return new MemberProfileResponse(result.id(), result.nickname());
    }

    public MemberTasteProfileSummaryResponse toMemberTasteProfileSummaryResponse(MemberTasteProfileSummaryResult result) {
        return new MemberTasteProfileSummaryResponse(
                result.memberId(),
                result.profileVersion(),
                result.attributeCategories().stream()
                        .map(item -> new MemberTasteAttributeCategoryResponse(
                                item.id(),
                                item.categoryType(),
                                item.code(),
                                item.name(),
                                item.sortOrder()
                        ))
                        .toList(),
                result.restrictionIngredients().stream()
                        .map(item -> new MemberTasteRestrictionIngredientResponse(
                                item.id(),
                                item.code(),
                                item.name(),
                                item.allergen(),
                                item.sortOrder()
                        ))
                        .toList(),
                result.updatedAt()
        );
    }

    public UpdateMemberBasicInfoCommand toUpdateMemberBasicInfoCommand(String nickname) {
        return new UpdateMemberBasicInfoCommand(nickname);
    }

    public UpdateMemberTasteProfileCommand toUpdateMemberTasteProfileCommand(String profileVersion) {
        return new UpdateMemberTasteProfileCommand(profileVersion);
    }

    public UpdateMemberResponse toUpdateMemberResponse(UpdateMemberResult result) {
        return new UpdateMemberResponse(result.id(), result.updatedAt());
    }

    public WithdrawMemberResponse toWithdrawMemberResponse(WithdrawMemberResult result) {
        return new WithdrawMemberResponse(result.id(), result.status());
    }
}
