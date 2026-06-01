package matchuri.backend.domain.menu.result;

public record AdminMenuItemDetailResult(
        Long id,
        String code,
        String name,
        String description,
        boolean isActive,
        String thumbnailUrl
) {
}
