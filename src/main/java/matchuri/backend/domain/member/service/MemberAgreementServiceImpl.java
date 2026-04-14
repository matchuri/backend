package matchuri.backend.domain.member.service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.service.IssuedAccessToken;
import matchuri.backend.domain.auth.service.JwtTokenProvider;
import matchuri.backend.domain.member.MemberErrorCode;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.AuthenticatedMember;
import matchuri.backend.global.security.AuthenticationFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAgreementServiceImpl implements MemberAgreementService {

    private final MemberAgreementRepository memberAgreementRepository;
    private final MemberRepository memberRepository;
    private final RequiredAgreementRequestValidator requiredAgreementRequestValidator;
    private final AuthenticationFacade authenticationFacade;
    private final JwtTokenProvider jwtTokenProvider;
    private final RequiredAgreementRevisionResolver requiredAgreementRevisionResolver;

    @Override
    public RequiredAgreementStatusResult getRequiredAgreementStatus() {
        Member member = getCurrentActiveMember();
        return calculateStatus(member.getId());
    }

    @Override
    @Transactional
    public SubmitRequiredAgreementsResult submitRequiredAgreements(SubmitRequiredAgreementsCommand command) {
        Member member = getCurrentActiveMember();

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

        RequiredAgreementStatusResult status = calculateStatus(member.getId());
        IssuedAccessToken issuedAccessToken = jwtTokenProvider.issueAccessToken(member, RequiredAgreementVersions.currentRevision());
        return new SubmitRequiredAgreementsResult(status, issuedAccessToken);
    }

    @Override
    public boolean hasCompletedRequiredAgreements(Long memberId) {
        return calculateStatus(memberId).requiredAgreementsCompleted();
    }

    @Override
    public String resolveRequiredAgreementRevision(Long memberId) {
        return requiredAgreementRevisionResolver.resolve(memberId);
    }

    private RequiredAgreementStatusResult calculateStatus(Long memberId) {
        Map<AgreementType, String> agreedVersions = new EnumMap<>(AgreementType.class);
        memberAgreementRepository.findByMemberIdAndAgreementTypeIn(memberId, RequiredAgreementVersions.requiredTypes())
                .forEach(agreement -> agreedVersions.put(agreement.getAgreementType(), agreement.getAgreementVersion()));

        List<AgreementType> missingTypes = RequiredAgreementVersions.requiredTypes().stream()
                .filter(type -> !RequiredAgreementVersions.getRequiredVersion(type).equals(agreedVersions.get(type)))
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        return new RequiredAgreementStatusResult(missingTypes.isEmpty(), missingTypes);
    }

    private Member getCurrentActiveMember() {
        AuthenticatedMember authenticatedMember = authenticationFacade.getCurrentMember();
        Member member = memberRepository.findById(authenticatedMember.memberId())
                .orElseThrow(() -> new BusinessException(
                        MemberErrorCode.NOT_FOUND,
                        MemberErrorCode.NOT_FOUND.format(authenticatedMember.memberId())
                ));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    MemberErrorCode.INACTIVE_MEMBER,
                    MemberErrorCode.INACTIVE_MEMBER.format(member.getId())
            );
        }
        return member;
    }
}
