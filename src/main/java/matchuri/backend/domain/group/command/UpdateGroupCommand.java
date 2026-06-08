package matchuri.backend.domain.group.command;

import java.math.BigDecimal;

public record UpdateGroupCommand(
        Long groupId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer level,
        String address
) {

    public boolean hasNoFields() {
        return name == null && latitude == null && longitude == null && level == null && address == null;
    }
}
