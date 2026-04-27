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
                @Index(name = "idx_members_social_provider", columnList = "is_social,social_provider_type"),
                @Index(name = "idx_members_nickname", columnList = "nickname")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_members_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uk_members_nickname", columnNames = "nickname"),
                @UniqueConstraint(
                        name = "uk_members_social_provider_user",
                        columnNames = {"social_provider_type", "social_provider_user_id"}
                )
        }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    public static final int LOGIN_ID_MAX_SIZE = 50;
    public static final int PASSWORD_MIN_SIZE = 8;
    public static final int PASSWORD_MAX_SIZE = 100;
    public static final int NICKNAME_MAX_SIZE = 100;
    public static final String LOGIN_ID_PATTERN = "^[A-Za-z0-9._-]+$";
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 ID")
    private Long id;

    @Column(name = "login_id", length = LOGIN_ID_MAX_SIZE, comment = "로그인 아이디")
    private String loginId;

    @Column(name = "password_hash", comment = "비밀번호 해시")
    private String passwordHash;

    @Column(name = "nickname", length = NICKNAME_MAX_SIZE, comment = "닉네임 (자체 로그인은 수집)")
    private String nickname;

    @Builder.Default
    @Column(name = "nickname_completed", nullable = false, comment = "닉네임 온보딩 확인 여부")
    private boolean nicknameCompleted = false;

    @Column(length = 150, comment = "이메일")
    private String email;

    @Column(name = "is_social", nullable = false, comment = "소셜 로그인 여부")
    private boolean social;

    // 일반 로그인 계정은 제공자 정보가 없으므로 nullable로 둔다.
    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider_type", length = 20, comment = "소셜 제공자")
    private SocialProviderType socialProviderType;

    @Column(name = "social_provider_user_id", length = 100, comment = "소셜 제공자 사용자 식별자")
    private String socialProviderUserId;

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
            String socialProviderUserId,
            MemberRole memberRole,
            MemberStatus status
    ) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.email = email;
        this.social = social;
        this.socialProviderType = socialProviderType;
        this.socialProviderUserId = socialProviderUserId;
        this.memberRole = memberRole;
        this.status = status;
        this.nicknameCompleted = !social;
    }

    public static Member createWithEncodedPassword(String loginId, String passwordHash, String nickname, String email) {
        return Member.builder()
                .loginId(loginId)
                .passwordHash(passwordHash)
                .email(email)
                .nickname(nickname)
                .nicknameCompleted(true)
                .social(false)
                .socialProviderType(null)
                .socialProviderUserId(null)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    public static Member createSocialMember(SocialProviderType provider, String providerUserId, String email,
                                            String nickname) {
        return Member.builder()
                .loginId(null)
                .passwordHash(null)
                .email(email)
                .nickname(nickname)
                .nicknameCompleted(false)
                .social(true)
                .socialProviderType(provider)
                .socialProviderUserId(providerUserId)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    public void attachTasteProfile(MemberTasteProfile tasteProfile) {
        this.tasteProfile = tasteProfile;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameCompleted = true;
    }

    public void withdraw() {
        this.status = MemberStatus.INACTIVE;
    }
}
