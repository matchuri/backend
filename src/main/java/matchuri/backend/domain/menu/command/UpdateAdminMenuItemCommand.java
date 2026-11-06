package matchuri.backend.domain.menu.command;

public record UpdateAdminMenuItemCommand(
        Long menuItemId,
        String name,
        String description,
        Boolean isActive
) {
}
