package matchuri.backend.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DevSampleDataInitializer {

    private final MemberRepository memberRepository;
    private final MatchuriProperties matchuriProperties;

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
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        log.info("Sample member created. loginId={}", loginId);
        return 1;
    }
}
