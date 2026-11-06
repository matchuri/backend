package matchuri.backend.domain.member.support.deletion;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.service.MemberPurgeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPurgeScheduler {

    private static final int BATCH_SIZE = 100;

    private final MemberRepository memberRepository;
    private final MemberPurgeService memberPurgeService;

    @Scheduled(cron = "${matchuri.member-deletion.purge-cron:0 0 * * * *}", zone = "UTC")
    public void purgeDueMembers() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int purgedCount = 0;

        while (true) {
            List<Long> candidateIds = memberRepository.findPurgeCandidateIds(MemberStatus.DELETED, now, PageRequest.of(0, BATCH_SIZE));
            if (candidateIds.isEmpty()) {
                break;
            }

            int batchPurgedCount = 0;
            for (Long memberId : candidateIds) {
                try {
                    if (memberPurgeService.purgeIfDue(memberId, now)) {
                        batchPurgedCount++;
                    }
                } catch (RuntimeException exception) {
                    log.error("member deletion event=purge_failed memberId={}", memberId, exception);
                }
            }
            purgedCount += batchPurgedCount;

            if (candidateIds.size() < BATCH_SIZE || batchPurgedCount == 0) {
                break;
            }
        }

        if (purgedCount > 0) {
            log.info("member deletion event=purge_batch_completed count={}", purgedCount);
        }
    }
}
