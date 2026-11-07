package matchuri.backend.domain.member.repository;

import java.util.Optional;
import matchuri.backend.domain.member.entity.MemberProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileImageRepository extends JpaRepository<MemberProfileImage, Long>,
        MemberProfileImageRepositoryCustom {

    Optional<MemberProfileImage> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);
}
