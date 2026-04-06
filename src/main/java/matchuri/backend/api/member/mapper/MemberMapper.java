package matchuri.backend.api.member.mapper;

import java.util.List;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.member.dto.CreateMemberResponse;
import matchuri.backend.api.member.dto.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.MemberProfileResponse;
import matchuri.backend.api.member.dto.MemberTasteProfileSummaryResponse;
import matchuri.backend.api.member.dto.UpdateMemberResponse;
import matchuri.backend.api.member.dto.WithdrawMemberResponse;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public LoginIdExistsResponse toLoginIdExistsResponse(String loginId, boolean exists) {
        return new LoginIdExistsResponse(loginId, exists);
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

    public UpdateMemberResponse toUpdateMemberResponse(Member member) {
        return new UpdateMemberResponse(member.getId(), member.getUpdatedAt());
    }

    public WithdrawMemberResponse toWithdrawMemberResponse(Member member) {
        return new WithdrawMemberResponse(member.getId(), member.getStatus().name());
    }

    private MemberTasteProfileSummaryResponse toTasteProfileSummary(MemberTasteProfile tasteProfile) {
        if (tasteProfile == null) {
            return null;
        }

        return new MemberTasteProfileSummaryResponse(List.of(), List.of(), tasteProfile.getProfileVersion());
    }
}
