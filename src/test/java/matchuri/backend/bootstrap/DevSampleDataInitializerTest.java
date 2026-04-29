package matchuri.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.config.MatchuriProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DevSampleDataInitializerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    @Test
    @DisplayName("local 프로필이 아니면 고정 관리자 계정 시드를 생성하지 않는다")
    void skipsAdminSeedWhenLocalProfileIsNotActive() {
        MatchuriProperties properties = seedProperties(true, true);
        when(memberRepository.existsByLoginId("tester01")).thenReturn(false);
        when(memberRepository.existsByLoginId("tester02")).thenReturn(false);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        DevSampleDataInitializer initializer = new DevSampleDataInitializer(
                memberRepository,
                properties,
                passwordEncoder,
                environment
        );

        int createdCount = initializer.initialize();

        assertThat(createdCount).isEqualTo(2);
        verify(memberRepository, never()).existsByLoginId(DevSampleDataInitializer.ADMIN_LOGIN_ID);
        verify(passwordEncoder, never()).encode(anyString());

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository, times(2)).save(memberCaptor.capture());
        assertThat(memberCaptor.getAllValues())
                .extracting(Member::getLoginId)
                .containsExactly("tester01", "tester02");
    }

    @Test
    @DisplayName("local 프로필이면 Swagger 수동 테스트용 관리자 계정 시드를 생성한다")
    void createsAdminSeedWhenLocalProfileIsActive() {
        MatchuriProperties properties = seedProperties(true, true);
        when(memberRepository.existsByLoginId("tester01")).thenReturn(true);
        when(memberRepository.existsByLoginId("tester02")).thenReturn(true);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        when(memberRepository.existsByLoginId(DevSampleDataInitializer.ADMIN_LOGIN_ID)).thenReturn(false);
        when(passwordEncoder.encode(DevSampleDataInitializer.ADMIN_PASSWORD)).thenReturn("encoded-admin-password");

        DevSampleDataInitializer initializer = new DevSampleDataInitializer(
                memberRepository,
                properties,
                passwordEncoder,
                environment
        );

        int createdCount = initializer.initialize();

        assertThat(createdCount).isEqualTo(1);
        verify(memberRepository).existsByLoginId(eq(DevSampleDataInitializer.ADMIN_LOGIN_ID));
        verify(passwordEncoder).encode(DevSampleDataInitializer.ADMIN_PASSWORD);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member admin = memberCaptor.getValue();
        assertThat(admin.getLoginId()).isEqualTo(DevSampleDataInitializer.ADMIN_LOGIN_ID);
        assertThat(admin.getPasswordHash()).isEqualTo("encoded-admin-password");
        assertThat(admin.getMemberRole().name()).isEqualTo("ADMIN");
    }

    private MatchuriProperties seedProperties(boolean enabled, boolean sampleMembersEnabled) {
        MatchuriProperties properties = new MatchuriProperties();
        MatchuriProperties.Seed seed = new MatchuriProperties.Seed();
        seed.setEnabled(enabled);
        seed.setSampleMembersEnabled(sampleMembersEnabled);
        properties.setSeed(seed);
        return properties;
    }
}
