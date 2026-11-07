package matchuri.backend.domain.member.command;

import java.util.List;

public record RegisterLocalMemberCommand(
        String loginId,
        String password,
        String nickname,
        String email,
        String emailVerificationToken,
        List<SubmitRequiredAgreementsCommand.AgreementConsentCommand> agreements
) {
    public RegisterLocalMemberCommand {
        email = email == null ? null : email.trim().toLowerCase();
        emailVerificationToken = emailVerificationToken == null ? null : emailVerificationToken.trim();
    }
}
