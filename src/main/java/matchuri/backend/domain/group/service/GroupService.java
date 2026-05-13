package matchuri.backend.domain.group.service;

import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.result.CreateGroupResult;

public interface GroupService {

    CreateGroupResult createGroup(CreateGroupCommand command);
}
