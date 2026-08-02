package matchuri.backend.infra.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedDataResourceLoader {

    private final ObjectMapper objectMapper;

    public <T> T load(String path, Class<T> type) {
        ClassPathResource resource = new ClassPathResource(path);

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, type);
        } catch (IOException exception) {
            throw new IllegalStateException("Seed data resource를 읽을 수 없습니다. path=" + path, exception);
        }
    }
}
