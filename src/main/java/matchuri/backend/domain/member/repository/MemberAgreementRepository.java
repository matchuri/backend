package matchuri.backend.domain.member.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {

    List<MemberAgreement> findByMemberIdAndAgreementTypeIn(Long memberId, Collection<AgreementType> agreementTypes);

    boolean existsByMemberIdAndAgreementTypeAndAgreementVersion(Long memberId, AgreementType agreementType, String agreementVersion);
}
