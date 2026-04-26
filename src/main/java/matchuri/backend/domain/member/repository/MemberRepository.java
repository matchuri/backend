package matchuri.backend.domain.member.repository;

import java.util.Optional;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByEmailAndSocialFalseAndStatus(String email, MemberStatus status);

    boolean existsByLoginIdAndEmailAndSocialFalseAndStatus(String loginId, String email, MemberStatus status);

    boolean existsByIdAndNicknameCompletedTrue(Long memberId);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByEmailAndSocialFalseAndStatus(String email, MemberStatus status);

    Optional<Member> findByLoginIdAndEmailAndSocialFalseAndStatus(String loginId, String email, MemberStatus status);

    Optional<Member> findBySocialProviderTypeAndSocialProviderUserId(
            SocialProviderType socialProviderType,
            String socialProviderUserId
    );
}
