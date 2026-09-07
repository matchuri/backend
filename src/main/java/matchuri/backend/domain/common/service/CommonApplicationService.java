package matchuri.backend.domain.common.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.common.dto.response.HomeResponse;
import matchuri.backend.api.common.mapper.HomeMapper;
import matchuri.backend.domain.group.result.GroupHomeActivityResult;
import matchuri.backend.domain.group.service.GroupRecommendationService;
import matchuri.backend.domain.member.result.MemberHomeResult;
import matchuri.backend.domain.member.service.MemberService;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationHomeResult;
import matchuri.backend.domain.recommendation.service.RecommendationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommonApplicationService {

    private final MemberService memberService;
    private final RecommendationService recommendationService;
    private final GroupRecommendationService groupRecommendationService;
    private final HomeMapper homeMapper;

    @Transactional
    public HomeResponse getHome(Long memberId) {
        MemberHomeResult member = memberService.getHomeMember(memberId);
        PersonalRecommendationHomeResult recommendations = recommendationService.getHomeRecommendations(memberId);
        List<GroupHomeActivityResult> activities = groupRecommendationService.getHomeActivities(memberId);
        return homeMapper.toResponse(
                member.profile(),
                member.location(),
                member.tasteProfile(),
                recommendations,
                activities
        );
    }
}
