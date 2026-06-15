package matchuri.backend.global.util;

import java.util.Map;

public class TypeUtils {
    public static Map<?, ?> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }

        return Map.of();
    }

    public static String stringValue(Object value) {
        if (value == null) {
            return null;
        }

        String stringValue = String.valueOf(value);
        if (stringValue.isBlank()) {
            return null;
        }

        return stringValue;
    }

    public static String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}
