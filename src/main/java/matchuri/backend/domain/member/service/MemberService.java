package matchuri.backend.domain.member.service;

public interface MemberService {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    RegisterLocalMemberResult registerLocalMember(RegisterLocalMemberCommand command);

    CreateMemberResult createMember(CreateMemberCommand command);

    MemberProfileResult getMyProfile();

    UpdateMemberResult updateMyProfile(UpdateMemberBasicInfoCommand command);

    UpdateMemberResult updateMyTasteProfile(UpdateMemberTasteProfileCommand command);

    WithdrawMemberResult withdraw();
}
