package matchuri.backend.domain.recommendation.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecommendationLocationContextJsonFactoryTest {

    private final RecommendationLocationContextJsonFactory factory =
            new RecommendationLocationContextJsonFactory(new ObjectMapper());

    @Test
    @DisplayName("위치 필드가 모두 있으면 컨텍스트 JSON을 생성한다")
    void createContextJsonWhenLocationIsComplete() {
        Optional<String> contextJson = factory.createIfComplete(
                new BigDecimal("37.498095"),
                new BigDecimal("127.027610"),
                1000,
                "서울 강남구 테헤란로 123"
        );

        assertThat(contextJson).hasValueSatisfying(json -> {
            assertThat(json).contains("\"latitude\":37.498095");
            assertThat(json).contains("\"longitude\":127.027610");
            assertThat(json).contains("\"radiusMeters\":1000");
            assertThat(json).contains("\"address\":\"서울 강남구 테헤란로 123\"");
        });
    }

    @Test
    @DisplayName("위치 필드 중 하나라도 없거나 주소가 공백이면 컨텍스트 JSON을 생성하지 않는다")
    void doNotCreateContextJsonWhenLocationIsIncomplete() {
        BigDecimal latitude = new BigDecimal("37.498095");
        BigDecimal longitude = new BigDecimal("127.027610");

        assertThat(factory.createIfComplete(null, longitude, 1000, "서울 강남구")).isEmpty();
        assertThat(factory.createIfComplete(latitude, null, 1000, "서울 강남구")).isEmpty();
        assertThat(factory.createIfComplete(latitude, longitude, null, "서울 강남구")).isEmpty();
        assertThat(factory.createIfComplete(latitude, longitude, 1000, null)).isEmpty();
        assertThat(factory.createIfComplete(latitude, longitude, 1000, "  ")).isEmpty();
    }
}
