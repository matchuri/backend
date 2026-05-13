package matchuri.backend.api.group;

import matchuri.backend.api.group.dto.request.CreateGroupRequest;
import matchuri.backend.api.group.dto.response.CreateGroupResponse;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.result.CreateGroupResult;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public CreateGroupCommand toCreateGroupCommand(CreateGroupRequest request) {
        return new CreateGroupCommand(
                request.name(),
                request.latitude(),
                request.longitude()
        );
    }

    public CreateGroupResponse toCreateGroupResponse(CreateGroupResult result) {
        return new CreateGroupResponse(
                result.groupId(),
                result.status()
        );
    }
}
