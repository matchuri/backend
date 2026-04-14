package matchuri.backend.domain.member.service;

public interface MemberAgreementService {

    RequiredAgreementStatusResult getRequiredAgreementStatus();

    SubmitRequiredAgreementsResult submitRequiredAgreements(SubmitRequiredAgreementsCommand command);

    boolean hasCompletedRequiredAgreements(Long memberId);

    String resolveRequiredAgreementRevision(Long memberId);
}
