package matchuri.backend.api.group;

import matchuri.backend.api.group.dto.request.CreateGroupRequest;
import matchuri.backend.api.group.dto.response.CreateGroupResponse;
import matchuri.backend.api.group.dto.response.GroupDetailResponse;
import matchuri.backend.api.group.dto.response.GroupMemberSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupSummaryResponse;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupMemberSummaryResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
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
