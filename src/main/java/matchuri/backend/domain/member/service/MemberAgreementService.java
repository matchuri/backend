package matchuri.backend.domain.member.service;

public interface MemberAgreementService {

    RequiredAgreementStatusResult getRequiredAgreementStatus();

    RequiredAgreementStatusResult submitRequiredAgreements(SubmitRequiredAgreementsCommand command);

    boolean hasCompletedRequiredAgreements(Long memberId);
}
