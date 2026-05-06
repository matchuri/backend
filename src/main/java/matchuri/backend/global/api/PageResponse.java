package matchuri.backend.global.api;

import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private PageInfo pageInfo;

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), PageInfo.of(page));
    }

    public static <T, R> PageResponse<R> of(Page<T> page, Function<T, R> mapper) {
        List<R> mappedContent = page.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(mappedContent, PageInfo.of(page));
    }

    public static <T> PageResponse<T> mock(List<T> list) {
        return new PageResponse<>(list, PageInfo.mock());
    }
}