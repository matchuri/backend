package matchuri.backend.domain.menu.command;

public record UpdateAdminAttributeCategoryCommand(
        Long attributeCategoryId,
        String name,
        Integer sortOrder,
        Boolean isActive
) {
}
