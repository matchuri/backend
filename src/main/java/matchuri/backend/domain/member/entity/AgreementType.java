package matchuri.backend.domain.member.entity;

import java.util.Arrays;

public enum AgreementType {
    TERMS_OF_SERVICE,
    PRIVACY_POLICY;

    public static AgreementType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(value))
                .findFirst()
                .orElse(null);
    }
}
