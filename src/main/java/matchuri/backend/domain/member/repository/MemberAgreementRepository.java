package matchuri.backend.domain.member.repository;

import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.MemberAgreement;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {

    boolean existsByMemberIdAndAgreementTypeAndAgreementVersion(Long memberId, AgreementType agreementType,
                                                                String agreementVersion);
}
