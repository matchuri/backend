package matchuri.backend.domain.group.service;

import java.util.Optional;
import lombok.NonNull;
import matchuri.backend.domain.group.command.CreateNicknameGroupInviteCommand;
import matchuri.backend.domain.group.command.GetMyGroupInvitesCommand;
import matchuri.backend.domain.group.command.JoinGroupCommand;
import matchuri.backend.domain.group.command.RespondGroupInviteCommand;
import matchuri.backend.domain.group.result.CreateNicknameGroupInviteResult;
import matchuri.backend.domain.group.result.GroupInviteLinkResult;
import matchuri.backend.domain.group.result.GroupInviteLinkPreviewResult;
import matchuri.backend.domain.group.result.GroupInviteSummaryResult;
import matchuri.backend.domain.group.result.GroupInviteV2SummaryResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.RespondGroupInviteResult;
import org.springframework.data.domain.Page;

public interface GroupInviteService {

    CreateNicknameGroupInviteResult createNicknameInvite(Long memberId, CreateNicknameGroupInviteCommand command);

    GroupInviteLinkResult createInviteLink(Long memberId, Long groupId);

    GroupInviteLinkResult reissueInviteLink(Long memberId, Long groupId);

    Optional<GroupInviteLinkResult> getCurrentInviteLink(Long memberId, Long groupId);

    GroupInviteLinkPreviewResult previewInviteLink(String token);

    JoinGroupResult joinGroupByInviteLink(Long memberId, String token);

    JoinGroupResult joinGroup(Long memberId, JoinGroupCommand command);

    Page<@NonNull GroupInviteSummaryResult> getMyInvites(Long memberId, GetMyGroupInvitesCommand command);

    Page<@NonNull GroupInviteV2SummaryResult> getMyInvitesV2(Long memberId, GetMyGroupInvitesCommand command);

    boolean existsMyPendingInvite(Long memberId);

    RespondGroupInviteResult respondGroupInvite(Long memberId, RespondGroupInviteCommand command);
}
