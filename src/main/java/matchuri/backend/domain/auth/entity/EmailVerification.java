package matchuri.backend.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(name = "email_verification")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "code")
    private String code;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private EmailVerificationType type;

    public static EmailVerification from(String email, String code, EmailVerificationType type) {
        return new EmailVerification(null, email, code, type);
    }
}
