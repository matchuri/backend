package matchuri.backend.catalog.command;

public record GetRestrictionIngredientsCommand(
        String query,
        Boolean allergen
) {
}
