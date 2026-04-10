package matchuri.backend.api.memberagreement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.memberagreement.dto.RequiredAgreementStatusResponse;
import matchuri.backend.api.memberagreement.dto.SubmitRequiredAgreementsRequest;
import matchuri.backend.domain.member.service.MemberAgreementService;
import matchuri.backend.global.api.ApiResponse;
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
    public ApiResponse<RequiredAgreementStatusResponse> getRequiredAgreementStatus() {
        return ApiResponse.success(
                memberAgreementMapper.toResponse(memberAgreementService.getRequiredAgreementStatus())
        );
    }

    @Override
    @PostMapping("/consents")
    public ApiResponse<RequiredAgreementStatusResponse> submitRequiredAgreements(
            @Valid @RequestBody SubmitRequiredAgreementsRequest request
    ) {
        return ApiResponse.success(
                memberAgreementMapper.toResponse(
                        memberAgreementService.submitRequiredAgreements(memberAgreementMapper.toCommand(request))
                )
        );
    }
}
