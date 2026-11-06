package matchuri.backend.domain.group.support.location;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupLocation;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.repository.GroupLocationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupLocationManager {

    private final GroupLocationRepository groupLocationRepository;
    private final ObjectMapper objectMapper;

    public String toRecommendationContextJson(GroupRoom room) {
        Map<String, Object> context = new LinkedHashMap<>();
        GroupLocation location = latestGroupLocation(room.getId());
        if (location == null) {
            return "{}";
        }
        if (location.getLatitude() != null) {
            context.put("latitude", location.getLatitude());
        }
        if (location.getLongitude() != null) {
            context.put("longitude", location.getLongitude());
        }
        if (location.getRadiusMeters() != null) {
            context.put("radiusMeters", location.getRadiusMeters());
        }
        if (location.getAddress() != null) {
            context.put("address", location.getAddress());
        }

        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("그룹 추천 컨텍스트 정보를 JSON으로 변환할 수 없습니다.", exception);
        }
    }

    public void updateRoomLocationFromContextJson(GroupRoom room, String contextJson) {
        if (contextJson == null || contextJson.isBlank()) {
            return;
        }

        try {
            JsonNode contextNode = objectMapper.readTree(contextJson);
            if (contextNode == null || !contextNode.isObject()) {
                return;
            }

            BigDecimal latitude = toLocationDecimal(contextNode.get("latitude"), new BigDecimal("-90"),
                    new BigDecimal("90"));
            BigDecimal longitude = toLocationDecimal(contextNode.get("longitude"), new BigDecimal("-180"),
                    new BigDecimal("180"));
            Integer radiusMeters = toRadiusMeters(contextNode.get("radiusMeters"));
            String address = toLocationAddress(contextNode.get("address"));
            updateLatestGroupLocation(room, latitude, longitude, radiusMeters, address);
        } catch (JsonProcessingException ignored) {
            // contextJson is an open-ended recommendation snapshot; invalid location data must not break creation.
        }
    }

    public GroupLocation updateLatestGroupLocation(
            GroupRoom room,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            String address
    ) {
        if (latitude == null && longitude == null && radiusMeters == null && address == null) {
            return latestGroupLocation(room.getId());
        }

        GroupLocation location = latestGroupLocation(room.getId());
        if (location == null) {
            return groupLocationRepository.save(new GroupLocation(room, latitude, longitude, radiusMeters, address));
        }

        location.update(latitude, longitude, radiusMeters, address);
        return location;
    }

    public GroupLocation latestGroupLocation(Long roomId) {
        return groupLocationRepository.findFirstByRoomIdOrderByCreatedAtDescIdDesc(roomId).orElse(null);
    }

    private BigDecimal toLocationDecimal(JsonNode valueNode, BigDecimal min, BigDecimal max) {
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }

        try {
            BigDecimal value = valueNode.isNumber()
                    ? valueNode.decimalValue()
                    : new BigDecimal(valueNode.asText());
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                return null;
            }

            return value;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer toRadiusMeters(JsonNode valueNode) {
        if (valueNode == null || valueNode.isNull() || !valueNode.canConvertToInt()) {
            return null;
        }

        int value = valueNode.intValue();
        return value >= 0 ? value : null;
    }

    private String toLocationAddress(JsonNode valueNode) {
        if (valueNode == null || valueNode.isNull() || !valueNode.isTextual()) {
            return null;
        }

        String address = valueNode.textValue();
        if (address.isBlank()) {
            return null;
        }

        return address;
    }
}


