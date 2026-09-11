package matchuri.backend.domain.member.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@NullMarked
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByIdAndNicknameCompletedTrue(Long memberId);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByEmailAndSocialFalseAndStatus(String email, MemberStatus status);

    Optional<Member> findByLoginIdAndEmailAndSocialFalseAndStatus(String loginId, String email, MemberStatus status);

    Optional<Member> findBySocialProviderTypeAndSocialProviderUserId(SocialProviderType socialProviderType, String socialProviderUserId);

    boolean existsByEmailAndSocialFalse(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select member from Member member where member.id = :memberId")
    Optional<Member> findByIdForUpdate(@Param("memberId") Long memberId);

    @Query("""
            select member.id from Member member
            where member.status = :status
              and member.purgeAt <= :now
            order by member.purgeAt asc, member.id asc
            """)
    List<Long> findPurgeCandidateIds(@Param("status") MemberStatus status, @Param("now") LocalDateTime now, Pageable pageable);
}
