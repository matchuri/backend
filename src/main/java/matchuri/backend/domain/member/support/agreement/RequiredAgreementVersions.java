package matchuri.backend.domain.member.support.agreement;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import matchuri.backend.domain.member.entity.AgreementType;

public final class RequiredAgreementVersions {

    public static final String TERMS_OF_SERVICE_REQUIRED_VERSION = "2026-04-10";
    public static final String PRIVACY_POLICY_REQUIRED_VERSION = "2026-04-10";

    private static final Map<AgreementType, String> REQUIRED_VERSIONS = new EnumMap<>(AgreementType.class);
    private static final String CURRENT_REVISION;

    static {
        REQUIRED_VERSIONS.put(AgreementType.TERMS_OF_SERVICE, TERMS_OF_SERVICE_REQUIRED_VERSION);
        REQUIRED_VERSIONS.put(AgreementType.PRIVACY_POLICY, PRIVACY_POLICY_REQUIRED_VERSION);
        CURRENT_REVISION = REQUIRED_VERSIONS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey().name() + ":" + entry.getValue())
                .collect(Collectors.joining("|"));
    }

    private RequiredAgreementVersions() {
    }

    public static Set<AgreementType> requiredTypes() {
        return REQUIRED_VERSIONS.keySet();
    }

    public static String getRequiredVersion(AgreementType agreementType) {
        return REQUIRED_VERSIONS.get(agreementType);
    }

    public static String currentRevision() {
        return CURRENT_REVISION;
    }
}
