package matchuri.backend.domain.member.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.result.IssuedAccessToken;
import matchuri.backend.domain.auth.support.token.JwtTokenProvider;
import matchuri.backend.domain.member.command.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.result.RequiredAgreementStatusResult;
import matchuri.backend.domain.member.result.SubmitRequiredAgreementsResult;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRequestValidator;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRevisionResolver;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAgreementServiceImpl implements MemberAgreementService {

    private final MemberAgreementRepository memberAgreementRepository;
    private final RequiredAgreementRequestValidator requiredAgreementRequestValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final RequiredAgreementRevisionResolver requiredAgreementRevisionResolver;
    private final ActiveMemberReader activeMemberReader;

    @Override
    public RequiredAgreementStatusResult getRequiredAgreementStatus() {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        return requiredAgreementRevisionResolver.calculateStatus(member.getId());
    }

    @Override
    @Transactional
    public SubmitRequiredAgreementsResult submitRequiredAgreements(SubmitRequiredAgreementsCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();

        Map<AgreementType, String> requestedVersions = requiredAgreementRequestValidator.validateAndIndex(command.agreements());
        for (AgreementType requiredType : RequiredAgreementVersions.requiredTypes()) {
            String requiredVersion = RequiredAgreementVersions.getRequiredVersion(requiredType);
            if (!memberAgreementRepository.existsByMemberIdAndAgreementTypeAndAgreementVersion(
                    member.getId(),
                    requiredType,
                    requiredVersion
            )) {
                memberAgreementRepository.save(MemberAgreement.create(member, requiredType, requestedVersions.get(requiredType)));
            }
        }

        RequiredAgreementStatusResult status = requiredAgreementRevisionResolver.calculateStatus(member.getId());
        IssuedAccessToken issuedAccessToken = jwtTokenProvider.issueAccessToken(member, RequiredAgreementVersions.currentRevision());
        return new SubmitRequiredAgreementsResult(status, issuedAccessToken);
    }

    @Override
    public boolean hasCompletedRequiredAgreements(Long memberId) {
        return requiredAgreementRevisionResolver.calculateStatus(memberId).requiredAgreementsCompleted();
    }

    @Override
    public String resolveRequiredAgreementRevision(Long memberId) {
        return requiredAgreementRevisionResolver.resolve(memberId);
    }
}
