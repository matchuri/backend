package matchuri.backend.domain.menu.repository;

public record MenuItemDetailRow(
        Long id,
        String code,
        String name,
        String description,
        String thumbnailObjectKey
) {
}
