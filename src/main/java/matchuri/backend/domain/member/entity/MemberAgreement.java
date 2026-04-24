package matchuri.backend.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "member_agreements",
        comment = "회원 약관 동의 이력",
        indexes = {
                @Index(name = "idx_member_agreements_member_id", columnList = "member_id"),
                @Index(name = "idx_member_agreements_member_type", columnList = "member_id,agreement_type")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_agreements_member_type_version",
                        columnNames = {"member_id", "agreement_type", "agreement_version"}
                )
        }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 약관 동의 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 50, comment = "약관 종류")
    private AgreementType agreementType;

    @Column(name = "agreement_version", nullable = false, length = 50, comment = "약관 버전")
    private String agreementVersion;

    @Column(name = "agreed_at", nullable = false, comment = "동의 시각")
    private LocalDateTime agreedAt;

    public static MemberAgreement create(Member member, AgreementType agreementType, String agreementVersion) {
        return MemberAgreement.builder()
                .member(member)
                .agreementType(agreementType)
                .agreementVersion(agreementVersion)
                .agreedAt(LocalDateTime.now())
                .build();
    }
}
