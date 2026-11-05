package matchuri.backend.catalog.command;

import java.util.List;
import matchuri.backend.catalog.entity.CategoryType;

public record GetAttributeCategoriesCommand(
        List<CategoryType> categoryTypes
) {
}
