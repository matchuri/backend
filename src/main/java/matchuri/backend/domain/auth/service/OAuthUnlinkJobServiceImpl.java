package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.OAuthUnlinkJob;
import matchuri.backend.domain.auth.repository.OAuthUnlinkJobRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.MemberReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthUnlinkJobServiceImpl {

    private final MemberReader memberReader;
    private final OAuthUnlinkJobRepository oAuthUnlinkJobRepository;

    @Transactional
    public void recordFailureJob(Long memberId, String eName, String eMessage) {
        Member member = memberReader.get(memberId);

        OAuthUnlinkJob oAuthUnlinkJob = OAuthUnlinkJob.builder()
                .member(member)
                .errorType(eName)
                .errorMessage(eMessage)
                .build();

        oAuthUnlinkJobRepository.save(oAuthUnlinkJob);
    }
}
