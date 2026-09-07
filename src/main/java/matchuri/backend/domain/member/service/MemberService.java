package matchuri.backend.domain.member.service;

import java.util.List;
import matchuri.backend.domain.member.command.CreateMemberCommand;
import matchuri.backend.domain.member.command.PutMemberLocationCommand;
import matchuri.backend.domain.member.command.RegisterLocalMemberCommand;
import matchuri.backend.domain.member.command.RegisterLocalMemberV2Command;
import matchuri.backend.domain.member.command.UpdateMemberBasicInfoCommand;
import matchuri.backend.domain.member.command.UpdateMemberPasswordCommand;
import matchuri.backend.domain.member.command.UpdateMemberTasteProfileCommand;
import matchuri.backend.domain.member.result.CreateMemberResult;
import matchuri.backend.domain.member.result.MemberProfileResult;
import matchuri.backend.domain.member.result.MemberHomeResult;
import matchuri.backend.domain.member.result.MemberPresetProfileImageResult;
import matchuri.backend.domain.member.result.MemberProfileImageResult;
import matchuri.backend.domain.member.result.MemberLocationResult;
import matchuri.backend.domain.member.result.MemberTasteProfileSummaryResult;
import matchuri.backend.domain.member.result.MemberTasteUpdateResult;
import matchuri.backend.domain.member.result.RegisterLocalMemberResult;
import matchuri.backend.domain.member.result.UpdateMemberPasswordResult;
import matchuri.backend.domain.member.result.UpdateMemberResult;
import matchuri.backend.domain.member.result.WithdrawMemberResult;
import org.jspecify.annotations.Nullable;

public interface MemberService {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    RegisterLocalMemberResult registerLocalMember(RegisterLocalMemberCommand command);

    RegisterLocalMemberResult registerLocalMemberV2(RegisterLocalMemberV2Command command);

    CreateMemberResult createMember(CreateMemberCommand command);

    MemberProfileResult getMyProfile(Long memberId);

    MemberHomeResult getHomeMember(Long memberId);

    List<MemberPresetProfileImageResult> getPresetProfileImages(Long memberId);

    MemberProfileImageResult setPresetProfileImage(Long memberId, Long presetProfileImageId);

    @Nullable MemberLocationResult getMyLocation(Long memberId);

    MemberLocationResult putMyLocation(Long memberId, PutMemberLocationCommand command);

    MemberTasteProfileSummaryResult getMyTasteProfile(Long memberId);

    UpdateMemberResult updateMyProfile(Long memberId, UpdateMemberBasicInfoCommand command);

    UpdateMemberPasswordResult updateMyPassword(Long memberId, UpdateMemberPasswordCommand command);

    MemberTasteUpdateResult updateMyTasteProfile(Long memberId, UpdateMemberTasteProfileCommand command);

    WithdrawMemberResult withdraw(Long memberId);
}
