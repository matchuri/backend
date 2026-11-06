package matchuri.backend.domain.menu.repository;

public record MenuRecommendationRow(
        Long menuId,
        String menuCode,
        String menuName
) {
}
