package matchuri.backend.domain.member.command;

public record RegisterLocalMemberV2Command(
        RegisterLocalMemberCommand member,
        UpdateMemberTasteProfileCommand tasteProfile
) {
}
