package matchuri.backend.domain.member.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import matchuri.backend.domain.member.MemberAgreementErrorCode;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class RequiredAgreementRequestValidator {

    public Map<AgreementType, String> validateAndIndex(List<SubmitRequiredAgreementsCommand.AgreementConsentCommand> agreements) {
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
}
