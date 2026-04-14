package matchuri.backend.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
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
class MemberAgreementRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAgreementRepository memberAgreementRepository;

    @Test
    @DisplayName("회원별 약관 타입과 버전 존재 여부를 조회할 수 있다")
    void existsByMemberIdAndAgreementTypeAndAgreementVersion() {
        Member member = memberRepository.save(new Member(
                "agreement-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.TERMS_OF_SERVICE, "2026-04-10"));
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.PRIVACY_POLICY, "2026-04-10"));

        boolean exists = memberAgreementRepository.existsByMemberIdAndAgreementTypeAndAgreementVersion(
                member.getId(),
                AgreementType.TERMS_OF_SERVICE,
                "2026-04-10"
        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("같은 회원의 같은 약관 타입과 버전 조합은 유일해야 한다")
    void memberAgreementMustBeUniquePerMemberTypeAndVersion() {
        Member member = memberRepository.save(new Member(
                "agreement-user-2",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        memberAgreementRepository.saveAndFlush(MemberAgreement.create(member, AgreementType.TERMS_OF_SERVICE, "2026-04-10"));

        assertThatThrownBy(() -> memberAgreementRepository.saveAndFlush(
                MemberAgreement.create(member, AgreementType.TERMS_OF_SERVICE, "2026-04-10")
        )).isInstanceOf(Exception.class);
    }
}
