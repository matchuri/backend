package matchuri.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import matchuri.backend.domain.member.repository.MemberRepository;

@SpringBootTest(properties = {
        "matchuri.seed.enabled=true",
        "matchuri.seed.sample-members-enabled=true"
})
@ActiveProfiles({"test", "local"})
class SeedDataInitializerTest {

    @Autowired
    private SeedDataInitializer seedDataInitializer;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("local 프로필에서는 샘플 회원 시드가 멱등하게 초기화된다")
    void initializesSampleMembersIdempotently() throws Exception {
        long initialCount = memberRepository.count();

        assertThat(memberRepository.existsByLoginId("tester01")).isTrue();
        assertThat(memberRepository.existsByLoginId("tester02")).isTrue();

        seedDataInitializer.run(new DefaultApplicationArguments(new String[0]));

        assertThat(memberRepository.count()).isEqualTo(initialCount);
        assertThat(memberRepository.existsByLoginId("tester01")).isTrue();
        assertThat(memberRepository.existsByLoginId("tester02")).isTrue();
    }
}
