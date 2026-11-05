package matchuri.backend.domain.recommendation.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecommendationContextTest {

    @Test
    @DisplayName("추천 컨텍스트는 식사 시간 없이도 생성할 수 있다")
    void emptyContextHasNoMealTime() {
        RecommendationContext context = RecommendationContext.empty();

        assertThat(context.mealTime()).isNull();
    }

    @Test
    @DisplayName("추천 컨텍스트는 요청별 식사 시간을 표현한다")
    void contextCanCarryMealTime() {
        RecommendationContext context = new RecommendationContext(MealTime.NIGHT_SNACK);

        assertThat(context.mealTime()).isEqualTo(MealTime.NIGHT_SNACK);
    }
}
