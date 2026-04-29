package matchuri.backend.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DevSampleDataInitializer {

    static final String ADMIN_LOGIN_ID = "admin01";
    static final String ADMIN_PASSWORD = "Admin123!";

    private final MemberRepository memberRepository;
    private final MatchuriProperties matchuriProperties;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Transactional
    public int initialize() {
        if (!matchuriProperties.getSeed().isEnabled()) {
            log.info("Sample seed initialization skipped because matchuri.seed.enabled=false");
            return 0;
        }

        if (!matchuriProperties.getSeed().isSampleMembersEnabled()) {
            log.info("Sample member seed initialization skipped because matchuri.seed.sample-members-enabled=false");
            return 0;
        }

        int createdCount = 0;
        createdCount += createSampleMemberIfAbsent("tester01", "tester01@example.com");
        createdCount += createSampleMemberIfAbsent("tester02", "tester02@example.com");
        createdCount += createLocalAdminMemberIfAbsent();
        return createdCount;
    }

    private int createSampleMemberIfAbsent(String loginId, String email) {
        if (memberRepository.existsByLoginId(loginId)) {
            log.info("Sample member already exists. loginId={}", loginId);
            return 0;
        }

        memberRepository.save(new Member(
                loginId,
                "seed-password-hash",
                email,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        log.info("Sample member created. loginId={}", loginId);
        return 1;
    }

    private int createLocalAdminMemberIfAbsent() {
        if (!environment.acceptsProfiles(Profiles.of("local"))) {
            log.info("Sample admin member seed skipped because local profile is not active. loginId={}", ADMIN_LOGIN_ID);
            return 0;
        }

        if (memberRepository.existsByLoginId(ADMIN_LOGIN_ID)) {
            log.info("Sample admin member already exists. loginId={}", ADMIN_LOGIN_ID);
            return 0;
        }

        memberRepository.save(Member.builder()
                .loginId(ADMIN_LOGIN_ID)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .nickname("matchuri-admin")
                .nicknameCompleted(true)
                .email("admin01@example.com")
                .social(false)
                .socialProviderType(null)
                .socialProviderUserId(null)
                .memberRole(MemberRole.ADMIN)
                .status(MemberStatus.ACTIVE)
                .build());
        log.info("Sample admin member created. loginId={}", ADMIN_LOGIN_ID);
        return 1;
    }
}
