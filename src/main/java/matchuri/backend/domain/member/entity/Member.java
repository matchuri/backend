package matchuri.backend.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
    name = "members",
    comment = "회원",
    indexes = {
        @Index(name = "idx_members_email", columnList = "email"),
        @Index(name = "idx_members_social_provider", columnList = "is_social,social_provider_type")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_members_login_id", columnNames = "login_id")
    }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    public static final int LOGIN_ID_MAX_SIZE = 50;
    public static final String LOGIN_ID_PATTERN = "^[A-Za-z0-9._-]+$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 ID")
    private Long id;

    @Column(name = "login_id", nullable = false, length = LOGIN_ID_MAX_SIZE, comment = "로그인 아이디")
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255, comment = "비밀번호 해시")
    private String passwordHash;

    @Column(name = "nickname", length = 50, comment = "닉네임 (자체 로그인은 수집)")
    private String nickname;

    @Column(length = 150, comment = "이메일")
    private String email;

    @Column(name = "is_social", nullable = false, comment = "소셜 로그인 여부")
    private boolean social;

    // 일반 로그인 계정은 제공자 정보가 없으므로 nullable로 둔다.
    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider_type", length = 20, comment = "소셜 제공자")
    private SocialProviderType socialProviderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 20, comment = "회원 역할")
    private MemberRole memberRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "회원 상태")
    private MemberStatus status;

    @OneToOne(mappedBy = "member")
    private MemberTasteProfile tasteProfile;

    public Member(
        String loginId,
        String passwordHash,
        String email,
        boolean social,
        SocialProviderType socialProviderType,
        MemberRole memberRole,
        MemberStatus status
    ) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.email = email;
        this.social = social;
        this.socialProviderType = socialProviderType;
        this.memberRole = memberRole;
        this.status = status;
    }

    public void attachTasteProfile(MemberTasteProfile tasteProfile) {
        this.tasteProfile = tasteProfile;
    }
}
