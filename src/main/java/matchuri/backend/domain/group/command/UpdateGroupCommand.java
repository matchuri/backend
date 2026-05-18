package matchuri.backend.domain.group.command;

import java.math.BigDecimal;

public record UpdateGroupCommand(
        Long groupId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude
) {

    public boolean hasNoFields() {
        return name == null && latitude == null && longitude == null;
    }
}
