package matchuri.backend.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
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
    static final String SAMPLE_MEMBER_PASSWORD = "Password123!";
    private static final String LEGACY_SAMPLE_PASSWORD_HASH = "seed-password-hash";

    private final MemberRepository memberRepository;
    private final MemberAgreementRepository memberAgreementRepository;
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
        createdCount += createSampleMemberIfAbsent("tester01", "테스터일", "tester01@example.com");
        createdCount += createSampleMemberIfAbsent("tester02", "테스터이", "tester02@example.com");
        createdCount += createLocalAdminMemberIfAbsent();
        return createdCount;
    }

    private int createSampleMemberIfAbsent(String loginId, String nickname, String email) {
        return memberRepository.findByLoginId(loginId)
                .map(member -> updateSampleMemberOnboardingIfNeeded(member, nickname))
                .orElseGet(() -> createSampleMember(loginId, nickname, email));
    }

    private int createSampleMember(String loginId, String nickname, String email) {
        Member member = Member.createWithEncodedPassword(
                loginId,
                passwordEncoder.encode(SAMPLE_MEMBER_PASSWORD),
                nickname,
                email
        );
        memberRepository.save(member);
        createRequiredAgreementConsentsIfAbsent(member);
        log.info("Sample member created. loginId={}", loginId);
        return 1;
    }

    private int updateSampleMemberOnboardingIfNeeded(Member member, String nickname) {
        int updatedCount = 0;
        if (!member.isNicknameCompleted() || member.getNickname() == null) {
            member.updateNickname(nickname);
            updatedCount++;
        }

        if (LEGACY_SAMPLE_PASSWORD_HASH.equals(member.getPasswordHash())) {
            member.updatePasswordHash(passwordEncoder.encode(SAMPLE_MEMBER_PASSWORD));
            updatedCount++;
        }

        updatedCount += createRequiredAgreementConsentsIfAbsent(member);
        if (updatedCount == 0) {
            log.info("Sample member already exists. loginId={}", member.getLoginId());
        } else {
            log.info("Sample member onboarding seed updated. loginId={}, updatedCount={}",
                    member.getLoginId(), updatedCount);
        }

        return updatedCount;
    }

    private int createLocalAdminMemberIfAbsent() {
        if (!environment.acceptsProfiles(Profiles.of("local"))) {
            log.info("Sample admin member seed skipped because local profile is not active. loginId={}", ADMIN_LOGIN_ID);
            return 0;
        }

        if (memberRepository.existsByLoginId(ADMIN_LOGIN_ID)) {
            Member admin = memberRepository.findByLoginId(ADMIN_LOGIN_ID).orElseThrow();
            int updatedCount = createRequiredAgreementConsentsIfAbsent(admin);
            if (updatedCount == 0) {
                log.info("Sample admin member already exists. loginId={}", ADMIN_LOGIN_ID);
            } else {
                log.info("Sample admin member onboarding seed updated. loginId={}, updatedCount={}",
                        ADMIN_LOGIN_ID, updatedCount);
            }
            return updatedCount;
        }

        Member admin = Member.builder()
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
                .build();
        memberRepository.save(admin);
        createRequiredAgreementConsentsIfAbsent(admin);
        log.info("Sample admin member created. loginId={}", ADMIN_LOGIN_ID);
        return 1;
    }

    private int createRequiredAgreementConsentsIfAbsent(Member member) {
        int createdCount = 0;
        for (AgreementType agreementType : RequiredAgreementVersions.requiredTypes()) {
            String agreementVersion = RequiredAgreementVersions.getRequiredVersion(agreementType);
            if (!memberAgreementRepository.existsByMemberIdAndAgreementTypeAndAgreementVersion(
                    member.getId(),
                    agreementType,
                    agreementVersion
            )) {
                memberAgreementRepository.save(MemberAgreement.create(member, agreementType, agreementVersion));
                createdCount++;
            }
        }
        return createdCount;
    }
}
