package matchuri.backend.domain.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Optional<Member> findByActiveMemberByNickname(String nickname);

    Optional<MemberHomeRow> findHomeRowByMemberId(Long memberId);

    List<Long> findPurgeCandidateIds(MemberStatus status, LocalDateTime now, Pageable pageable);

    Optional<Member> findByIdForUpdate(Long memberId);
}
