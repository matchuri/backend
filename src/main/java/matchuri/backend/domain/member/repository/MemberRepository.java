package matchuri.backend.domain.member.repository;

import java.util.Optional;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByIdAndNicknameCompletedTrue(Long memberId);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findBySocialProviderTypeAndSocialProviderUserId(
            SocialProviderType socialProviderType,
            String socialProviderUserId
    );
}
