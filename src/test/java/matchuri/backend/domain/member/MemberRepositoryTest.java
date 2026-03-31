package matchuri.backend.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.global.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Test
    @DisplayName("loginId로 회원 존재 여부를 확인할 수 있다")
    void existsByLoginId() {
        Member member = memberRepository.save(
            new Member(
                "tester01",
                "hashed-password",
                "tester@example.com",
                false,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
            )
        );

        assertThat(memberRepository.existsByLoginId("tester01")).isTrue();
        assertThat(memberRepository.existsByLoginId("unknown")).isFalse();
        assertThat(member.getId()).isNotNull();
    }

    @Test
    @DisplayName("회원 취향 프로필을 회원 ID로 조회할 수 있다")
    void findTasteProfileByMemberId() {
        Member member = memberRepository.save(
            new Member(
                "tester02",
                "hashed-password",
                "tester2@example.com",
                false,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
            )
        );

        MemberTasteProfile profile = memberTasteProfileRepository.save(
            new MemberTasteProfile(member, "v1")
        );

        assertThat(memberTasteProfileRepository.findByMemberId(member.getId()))
            .isPresent()
            .get()
            .extracting(MemberTasteProfile::getProfileVersion)
            .isEqualTo("v1");
        assertThat(profile.getId()).isNotNull();
    }

    @Test
    @DisplayName("저장 시 auditing 필드가 채워진다")
    void auditingFieldsArePopulated() {
        Member member = memberRepository.save(
            new Member(
                "tester03",
                "hashed-password",
                "tester3@example.com",
                false,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
            )
        );

        assertThat(member.getCreatedAt()).isNotNull();
        assertThat(member.getUpdatedAt()).isNotNull();
    }
}
