package matchuri.backend.domain.auth.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.SocialProviderType;

@Getter
@Entity
@Table(
        name = "auth_exchange_codes",
        indexes = {
                @Index(name = "idx_auth_exchange_codes_code", columnList = "code", unique = true),
                @Index(name = "idx_auth_exchange_codes_member", columnList = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthExchangeCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProviderType provider;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    private AuthExchangeCode(Member member, SocialProviderType provider, String code, LocalDateTime expiresAt) {
        this.member = member;
        this.provider = provider;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public static AuthExchangeCode issue(Member member, SocialProviderType provider, String code, LocalDateTime expiresAt) {
        return new AuthExchangeCode(member, provider, code, expiresAt);
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
