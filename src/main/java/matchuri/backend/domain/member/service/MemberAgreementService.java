package matchuri.backend.domain.member.service;

import matchuri.backend.domain.member.command.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.result.RequiredAgreementStatusResult;
import matchuri.backend.domain.member.result.SubmitRequiredAgreementsResult;

public interface MemberAgreementService {

    RequiredAgreementStatusResult getRequiredAgreementStatus();

    SubmitRequiredAgreementsResult submitRequiredAgreements(SubmitRequiredAgreementsCommand command);

    boolean hasCompletedRequiredAgreements(Long memberId);

    String resolveRequiredAgreementRevision(Long memberId);
}
