package matchuri.backend.domain.recommendation.repository;

import java.util.List;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import org.springframework.data.domain.Pageable;

public interface PersonalRecommendationRepositoryCustom {

    List<PersonalRecommendation> findRecentSelectedByMemberId(Long memberId, Pageable pageable);
}
