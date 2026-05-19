package matchuri.backend.api.group;

import matchuri.backend.api.group.dto.request.CreateGroupRequest;
import matchuri.backend.api.group.dto.request.CreateNicknameGroupInviteRequest;
import matchuri.backend.api.group.dto.request.JoinGroupRequest;
import matchuri.backend.api.group.dto.request.UpdateGroupRequest;
import matchuri.backend.api.group.dto.response.CreateNicknameGroupInviteResponse;
import matchuri.backend.api.group.dto.response.CreateGroupResponse;
import matchuri.backend.api.group.dto.response.DeleteGroupResponse;
import matchuri.backend.api.group.dto.response.GroupDetailResponse;
import matchuri.backend.api.group.dto.response.GroupMemberSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupSummaryResponse;
import matchuri.backend.api.group.dto.response.JoinGroupResponse;
import matchuri.backend.api.group.dto.response.LeaveGroupResponse;
import matchuri.backend.api.group.dto.response.UpdateGroupResponse;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.CreateNicknameGroupInviteCommand;
import matchuri.backend.domain.group.command.DeleteGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.command.JoinGroupCommand;
import matchuri.backend.domain.group.command.LeaveGroupCommand;
import matchuri.backend.domain.group.command.UpdateGroupCommand;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.CreateNicknameGroupInviteResult;
import matchuri.backend.domain.group.result.DeleteGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupMemberSummaryResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
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
                result.inviteCode(),
                result.status()
        );
    }

    public CreateNicknameGroupInviteCommand toCreateNicknameGroupInviteCommand(
            CreateNicknameGroupInviteRequest request
    ) {
        return new CreateNicknameGroupInviteCommand(
                request.groupId(),
                request.nickname()
        );
    }

    public CreateNicknameGroupInviteResponse toCreateNicknameGroupInviteResponse(
            CreateNicknameGroupInviteResult result
    ) {
        return new CreateNicknameGroupInviteResponse(
                result.inviteId(),
                result.groupId(),
                result.groupName(),
                result.targetMemberId(),
                result.targetNickname(),
                result.expiresAt(),
                result.status()
        );
    }

    public JoinGroupCommand toJoinGroupCommand(JoinGroupRequest request) {
        return new JoinGroupCommand(request.inviteCode());
    }

    public JoinGroupResponse toJoinGroupResponse(JoinGroupResult result) {
        return new JoinGroupResponse(
                result.groupId(),
                result.memberStatus()
        );
    }

    public LeaveGroupCommand toLeaveGroupCommand(Long groupId) {
        return new LeaveGroupCommand(groupId);
    }

    public LeaveGroupResponse toLeaveGroupResponse(LeaveGroupResult result) {
        return new LeaveGroupResponse(
                result.groupId(),
                result.memberStatus(),
                result.leftAt()
        );
    }

    public DeleteGroupCommand toDeleteGroupCommand(Long groupId) {
        return new DeleteGroupCommand(groupId);
    }

    public DeleteGroupResponse toDeleteGroupResponse(DeleteGroupResult result) {
        return new DeleteGroupResponse(
                result.groupId(),
                result.status(),
                result.deletedAt()
        );
    }

    public UpdateGroupCommand toUpdateGroupCommand(Long groupId, UpdateGroupRequest request) {
        return new UpdateGroupCommand(
                groupId,
                request.name(),
                request.latitude(),
                request.longitude()
        );
    }

    public UpdateGroupResponse toUpdateGroupResponse(UpdateGroupResult result) {
        return new UpdateGroupResponse(
                result.groupId(),
                result.name(),
                result.latitude(),
                result.longitude(),
                result.status(),
                result.updatedAt()
        );
    }

    public GetMyGroupsCommand toGetMyGroupsCommand(GroupRoomStatus status, int page, int size) {
        return new GetMyGroupsCommand(status, page, size);
    }

    public GroupSummaryResponse toGroupSummaryResponse(GroupSummaryResult result) {
        return new GroupSummaryResponse(
                result.id(),
                result.name(),
                result.status(),
                result.memberCount(),
                result.latestRecommendationStatus(),
                result.createdAt()
        );
    }

    public GroupDetailResponse toGroupDetailResponse(GroupDetailResult result) {
        return new GroupDetailResponse(
                result.id(),
                result.name(),
                result.inviteCode(),
                result.latitude(),
                result.longitude(),
                result.status(),
                result.members().stream()
                        .map(this::toGroupMemberSummaryResponse)
                        .toList(),
                null
        );
    }

    private GroupMemberSummaryResponse toGroupMemberSummaryResponse(GroupMemberSummaryResult result) {
        return new GroupMemberSummaryResponse(
                result.memberId(),
                result.nickname(),
                result.role(),
                result.status(),
                result.joinedAt()
        );
    }
}
