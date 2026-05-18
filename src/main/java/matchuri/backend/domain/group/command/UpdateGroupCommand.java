package matchuri.backend.domain.group.command;

public record UpdateGroupCommand(
        Long groupId,
        String name
) {

    public boolean hasNoFields() {
        return name == null;
    }
}
