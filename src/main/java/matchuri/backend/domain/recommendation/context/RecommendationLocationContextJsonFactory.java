package matchuri.backend.domain.recommendation.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationLocationContextJsonFactory {

    private final ObjectMapper objectMapper;

    public String create(
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            String address
    ) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("latitude", latitude);
        context.put("longitude", longitude);
        context.put("radiusMeters", radiusMeters);
        context.put("address", address);

        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("추천 위치 컨텍스트를 JSON으로 변환할 수 없습니다.", exception);
        }
    }
}
