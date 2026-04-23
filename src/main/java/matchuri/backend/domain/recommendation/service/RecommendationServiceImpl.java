package matchuri.backend.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    @Override
    public void getPersonalRecommendation() {
        // 취향 프로필, 지난 내역 불러오기
        // 알레르기 식품과 비선호 식품은 완전히 배제
        // 선호 카테고리 종합
        // 이전 추천 내역 확인 (다양한 메뉴를 추천하기 위해)
    }
}
