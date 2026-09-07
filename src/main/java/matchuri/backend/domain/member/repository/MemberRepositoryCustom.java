package matchuri.backend.domain.member.repository;

import java.util.Optional;
import matchuri.backend.domain.member.entity.Member;

public interface MemberRepositoryCustom {

    Optional<Member> findByActiveMemberByNickname(String nickname);

    Optional<MemberHomeQueryResult> findHomeQueryResultByMemberId(Long memberId);
}
