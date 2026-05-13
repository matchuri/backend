package matchuri.backend.domain.group.service;

import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import org.springframework.data.domain.Page;

public interface GroupService {

    CreateGroupResult createGroup(CreateGroupCommand command);

    Page<GroupSummaryResult> getMyGroups(GetMyGroupsCommand command);
}
