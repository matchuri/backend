package matchuri.backend.domain.member.service;

import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequiredAgreementRevisionResolver {

    private final MemberAgreementRepository memberAgreementRepository;

    public String resolve(Long memberId) {
        Map<AgreementType, String> agreedVersions = new EnumMap<>(AgreementType.class);
        memberAgreementRepository.findByMemberIdAndAgreementTypeIn(memberId, RequiredAgreementVersions.requiredTypes())
                .forEach(agreement -> agreedVersions.put(agreement.getAgreementType(), agreement.getAgreementVersion()));

        boolean completed = RequiredAgreementVersions.requiredTypes().stream()
                .allMatch(type -> RequiredAgreementVersions.getRequiredVersion(type).equals(agreedVersions.get(type)));

        return completed ? RequiredAgreementVersions.currentRevision() : null;
    }
}
