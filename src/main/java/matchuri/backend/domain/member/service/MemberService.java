package matchuri.backend.domain.member.service;

import matchuri.backend.domain.member.command.CreateMemberCommand;
import matchuri.backend.domain.member.command.PutMemberLocationCommand;
import matchuri.backend.domain.member.command.RegisterLocalMemberCommand;
import matchuri.backend.domain.member.command.UpdateMemberBasicInfoCommand;
import matchuri.backend.domain.member.command.UpdateMemberPasswordCommand;
import matchuri.backend.domain.member.command.UpdateMemberTasteProfileCommand;
import matchuri.backend.domain.member.result.CreateMemberResult;
import matchuri.backend.domain.member.result.MemberProfileResult;
import matchuri.backend.domain.member.result.MemberLocationResult;
import matchuri.backend.domain.member.result.MemberTasteProfileSummaryResult;
import matchuri.backend.domain.member.result.MemberTasteUpdateResult;
import matchuri.backend.domain.member.result.RegisterLocalMemberResult;
import matchuri.backend.domain.member.result.UpdateMemberPasswordResult;
import matchuri.backend.domain.member.result.UpdateMemberResult;
import matchuri.backend.domain.member.result.WithdrawMemberResult;

public interface MemberService {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    RegisterLocalMemberResult registerLocalMember(RegisterLocalMemberCommand command);

    CreateMemberResult createMember(CreateMemberCommand command);

    MemberProfileResult getMyProfile();

    MemberLocationResult getMyLocation();

    MemberLocationResult putMyLocation(PutMemberLocationCommand command);

    MemberTasteProfileSummaryResult getMyTasteProfile();

    UpdateMemberResult updateMyProfile(UpdateMemberBasicInfoCommand command);

    UpdateMemberPasswordResult updateMyPassword(UpdateMemberPasswordCommand command);

    MemberTasteUpdateResult updateMyTasteProfile(UpdateMemberTasteProfileCommand command);

    WithdrawMemberResult withdraw();
}
