package matchuri.backend.domain.member.service;

public interface MemberService {

    boolean existsByLoginId(String loginId);
}
