package matchuri.backend.domain.menu.result;

import matchuri.backend.domain.menu.entity.MenuItem;

public record MenuItemSummaryResult(
        Long id,
        String code,
        String name
) {

    public static MenuItemSummaryResult from(MenuItem menuItem) {
        return new MenuItemSummaryResult(
                menuItem.getId(),
                menuItem.getCode(),
                menuItem.getName()
        );
    }
}
