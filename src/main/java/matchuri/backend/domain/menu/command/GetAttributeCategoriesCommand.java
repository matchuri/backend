package matchuri.backend.domain.menu.command;

import java.util.List;
import matchuri.backend.domain.menu.entity.CategoryType;

public record GetAttributeCategoriesCommand(
        List<CategoryType> categoryTypes
) {
}
