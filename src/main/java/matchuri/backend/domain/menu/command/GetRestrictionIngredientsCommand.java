package matchuri.backend.domain.menu.command;

public record GetRestrictionIngredientsCommand(
        String query,
        Boolean allergen
) {
}
