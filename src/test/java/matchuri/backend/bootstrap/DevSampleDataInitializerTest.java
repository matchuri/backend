package matchuri.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
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
    private MemberAgreementRepository memberAgreementRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    @Test
    @DisplayName("local 프로필이 아니면 고정 관리자 계정 시드를 생성하지 않는다")
    void skipsAdminSeedWhenLocalProfileIsNotActive() {
        MatchuriProperties properties = seedProperties(true, true);
        when(memberRepository.findByLoginId("tester01")).thenReturn(Optional.empty());
        when(memberRepository.findByLoginId("tester02")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(DevSampleDataInitializer.SAMPLE_MEMBER_PASSWORD))
                .thenReturn("encoded-sample-password");
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        DevSampleDataInitializer initializer = new DevSampleDataInitializer(
                memberRepository,
                memberAgreementRepository,
                properties,
                passwordEncoder,
                environment
        );

        int createdCount = initializer.initialize();

        assertThat(createdCount).isEqualTo(2);
        verify(memberRepository, never()).existsByLoginId(DevSampleDataInitializer.ADMIN_LOGIN_ID);
        verify(passwordEncoder, times(2)).encode(DevSampleDataInitializer.SAMPLE_MEMBER_PASSWORD);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository, times(2)).save(memberCaptor.capture());
        assertThat(memberCaptor.getAllValues())
                .extracting(Member::getLoginId)
                .containsExactly("tester01", "tester02");
        assertThat(memberCaptor.getAllValues())
                .extracting(Member::getNickname)
                .containsExactly("테스터일", "테스터이");
        verify(memberAgreementRepository, times(RequiredAgreementVersions.requiredTypes().size() * 2))
                .save(any(MemberAgreement.class));
    }

    @Test
    @DisplayName("local 프로필이면 Swagger 수동 테스트용 관리자 계정 시드를 생성한다")
    void createsAdminSeedWhenLocalProfileIsActive() {
        MatchuriProperties properties = seedProperties(true, true);
        when(memberRepository.findByLoginId("tester01")).thenReturn(Optional.of(sampleMember("tester01", "테스터일")));
        when(memberRepository.findByLoginId("tester02")).thenReturn(Optional.of(sampleMember("tester02", "테스터이")));
        for (AgreementType agreementType : RequiredAgreementVersions.requiredTypes()) {
            when(memberAgreementRepository.existsByMemberIdAndAgreementTypeAndAgreementVersion(
                    any(),
                    eq(agreementType),
                    eq(RequiredAgreementVersions.getRequiredVersion(agreementType))
            )).thenReturn(true);
        }
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        when(memberRepository.existsByLoginId(DevSampleDataInitializer.ADMIN_LOGIN_ID)).thenReturn(false);
        when(passwordEncoder.encode(DevSampleDataInitializer.ADMIN_PASSWORD)).thenReturn("encoded-admin-password");

        DevSampleDataInitializer initializer = new DevSampleDataInitializer(
                memberRepository,
                memberAgreementRepository,
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

    private Member sampleMember(String loginId, String nickname) {
        return Member.createWithEncodedPassword(loginId, "encoded-sample-password", nickname, loginId + "@example.com");
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
