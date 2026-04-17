package matchuri.backend.domain.member.support.agreement;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.result.RequiredAgreementStatusResult;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequiredAgreementRevisionResolver {

    private final MemberAgreementRepository memberAgreementRepository;

    public RequiredAgreementStatusResult calculateStatus(Long memberId) {
        List<AgreementType> missingTypes = RequiredAgreementVersions.requiredTypes().stream()
                .filter(type -> !memberAgreementRepository.existsByMemberIdAndAgreementTypeAndAgreementVersion(
                        memberId,
                        type,
                        RequiredAgreementVersions.getRequiredVersion(type)
                ))
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        return new RequiredAgreementStatusResult(missingTypes.isEmpty(), missingTypes);
    }

    public String resolve(Long memberId) {
        return calculateStatus(memberId).requiredAgreementsCompleted()
                ? RequiredAgreementVersions.currentRevision()
                : null;
    }
}
