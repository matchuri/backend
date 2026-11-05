package matchuri.backend.api.memberagreement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.memberagreement.dto.request.SubmitRequiredAgreementsRequest;
import matchuri.backend.api.memberagreement.dto.response.RequiredAgreementStatusResponse;
import matchuri.backend.api.memberagreement.dto.response.SubmitRequiredAgreementsResponse;
import matchuri.backend.api.memberagreement.mapper.MemberAgreementMapper;
import matchuri.backend.domain.member.service.MemberAgreementService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member-agreements")
public class MemberAgreementController implements MemberAgreementApi {

    private final MemberAgreementService memberAgreementService;
    private final MemberAgreementMapper memberAgreementMapper;

    @Override
    @GetMapping("/required-status")
    public ApiResponse<RequiredAgreementStatusResponse> getRequiredAgreementStatus(
            @AuthenticatedMemberId Long memberId
    ) {
        return ApiResponse.success(
                memberAgreementMapper.toResponse(memberAgreementService.getRequiredAgreementStatus(memberId))
        );
    }

    @Override
    @PostMapping("/consents")
    public ApiResponse<SubmitRequiredAgreementsResponse> submitRequiredAgreements(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody SubmitRequiredAgreementsRequest request
    ) {
        return ApiResponse.success(
                memberAgreementMapper.toResponse(
                        memberAgreementService.submitRequiredAgreements(
                                memberId,
                                memberAgreementMapper.toCommand(request)
                        )
                )
        );
    }
}
