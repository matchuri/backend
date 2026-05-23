package matchuri.backend.domain.group.command;

public record CreateGroupRecommendationCommand(
        Long groupId,
        String contextJson
) {
}
