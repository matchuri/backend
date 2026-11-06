package matchuri.backend.domain.group.service;

import lombok.NonNull;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.DeleteGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.command.LeaveGroupCommand;
import matchuri.backend.domain.group.command.UpdateGroupCommand;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.DeleteGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import org.springframework.data.domain.Page;

public interface GroupManagementService {

    CreateGroupResult createGroup(Long memberId, CreateGroupCommand command);

    LeaveGroupResult leaveGroup(Long memberId, LeaveGroupCommand command);

    DeleteGroupResult deleteGroup(Long memberId, DeleteGroupCommand command);

    UpdateGroupResult updateGroup(Long memberId, UpdateGroupCommand command);

    Page<@NonNull GroupSummaryResult> getMyGroups(Long memberId, GetMyGroupsCommand command);

    GroupDetailResult getGroup(Long memberId, Long groupId);

    GroupDetailResult getGroupV2(Long memberId, Long groupId);
}

