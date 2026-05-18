package matchuri.backend.domain.group.service;

import lombok.NonNull;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.CreateGroupInviteCommand;
import matchuri.backend.domain.group.command.DeleteGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.command.JoinGroupCommand;
import matchuri.backend.domain.group.command.LeaveGroupCommand;
import matchuri.backend.domain.group.command.UpdateGroupCommand;
import matchuri.backend.domain.group.result.CreateGroupInviteResult;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.DeleteGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import org.springframework.data.domain.Page;

public interface GroupService {

    CreateGroupResult createGroup(CreateGroupCommand command);

    CreateGroupInviteResult createInvite(CreateGroupInviteCommand command);

    JoinGroupResult joinGroup(JoinGroupCommand command);

    LeaveGroupResult leaveGroup(LeaveGroupCommand command);

    DeleteGroupResult deleteGroup(DeleteGroupCommand command);

    UpdateGroupResult updateGroup(UpdateGroupCommand command);

    Page<@NonNull GroupSummaryResult> getMyGroups(GetMyGroupsCommand command);

    GroupDetailResult getGroup(Long groupId);
}
