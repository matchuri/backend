package matchuri.backend.domain.member.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPurgeService {

    private final MemberRepository memberRepository;

    @Transactional
    public boolean purgeIfDue(Long memberId, LocalDateTime now) {
        Member member = memberRepository.findByIdForUpdate(memberId).orElse(null);
        if (member == null
                || member.getStatus() != MemberStatus.DELETED
                || member.getPurgeAt() == null
                || member.getPurgeAt().isAfter(now)) {
            return false;
        }

        memberRepository.delete(member);
        memberRepository.flush();
        return true;
    }
}
