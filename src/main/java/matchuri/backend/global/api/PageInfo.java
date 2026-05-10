package matchuri.backend.global.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지네이션 메타데이터입니다. 페이지 번호는 0부터 시작합니다.")
public class PageInfo {
    @Schema(description = "현재 페이지 번호입니다. 0부터 시작합니다.", example = "0")
    private int page;

    @Schema(description = "요청한 페이지 크기입니다.", example = "20")
    private int size;

    @Schema(description = "조회 조건에 해당하는 전체 요소 수입니다.", example = "1")
    private long totalElements;

    @Schema(description = "전체 페이지 수입니다.", example = "1")
    private int totalPages;

    @Schema(description = "첫 페이지 여부입니다.", example = "true")
    private boolean first;

    @Schema(description = "마지막 페이지 여부입니다.", example = "true")
    private boolean last;

    @Schema(description = "다음 페이지 존재 여부입니다.", example = "false")
    private boolean hasNext;

    @Schema(description = "이전 페이지 존재 여부입니다.", example = "false")
    private boolean hasPrevious;

    public static PageInfo of(Page<?> page) {
        return new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    public static PageInfo mock() {
        return mock(0, 20, 1L);
    }

    public static PageInfo mock(int page, int size, long totalElements) {
        int safeSize = Math.max(size, 1);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return new PageInfo(
                page,
                safeSize,
                totalElements,
                totalPages,
                first,
                last,
                !last,
                !first
        );
    }

    public static PageInfo ofList(int size) {
        return new PageInfo(
                0,
                size,
                size,
                size == 0 ? 0 : 1,
                true,
                true,
                false,
                false
        );
    }
}
