package matchuri.backend.domain.member.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.MemberAgreementErrorCode;
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
    private final AuthenticationFacade authenticationFacade;

    @Override
    public RequiredAgreementStatusResult getRequiredAgreementStatus() {
        Member member = getCurrentActiveMember();
        return calculateStatus(member.getId());
    }

    @Override
    @Transactional
    public RequiredAgreementStatusResult submitRequiredAgreements(SubmitRequiredAgreementsCommand command) {
        Member member = getCurrentActiveMember();

        Map<AgreementType, String> requestedVersions = validateAndIndex(command.agreements());
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

        return calculateStatus(member.getId());
    }

    @Override
    public boolean hasCompletedRequiredAgreements(Long memberId) {
        return calculateStatus(memberId).requiredAgreementsCompleted();
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

    private Map<AgreementType, String> validateAndIndex(List<SubmitRequiredAgreementsCommand.AgreementConsentCommand> agreements) {
        Map<AgreementType, String> indexed = new EnumMap<>(AgreementType.class);
        if (agreements != null) {
            for (SubmitRequiredAgreementsCommand.AgreementConsentCommand agreement : agreements) {
                AgreementType agreementType = AgreementType.from(agreement.agreementType());
                if (agreementType == null) {
                    throw new BusinessException(
                            MemberAgreementErrorCode.INVALID_TYPE,
                            MemberAgreementErrorCode.INVALID_TYPE.format(agreement.agreementType())
                    );
                }

                String requiredVersion = RequiredAgreementVersions.getRequiredVersion(agreementType);
                if (!requiredVersion.equals(agreement.agreementVersion())) {
                    throw new BusinessException(
                            MemberAgreementErrorCode.VERSION_MISMATCH,
                            MemberAgreementErrorCode.VERSION_MISMATCH.format(agreementType.name(), agreement.agreementVersion())
                    );
                }

                indexed.put(agreementType, agreement.agreementVersion());
            }
        }

        Set<AgreementType> requiredTypes = RequiredAgreementVersions.requiredTypes();
        List<AgreementType> missingTypes = new ArrayList<>(requiredTypes.stream()
                .filter(type -> !indexed.containsKey(type))
                .sorted(Comparator.comparing(Enum::name))
                .toList());
        if (!missingTypes.isEmpty()) {
            throw new BusinessException(
                    MemberAgreementErrorCode.REQUIRED_TYPES_MISSING,
                    MemberAgreementErrorCode.REQUIRED_TYPES_MISSING.format(missingTypes)
            );
        }

        return indexed;
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
