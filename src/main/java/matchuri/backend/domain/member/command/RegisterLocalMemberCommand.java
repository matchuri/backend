package matchuri.backend.domain.member.command;

import java.util.List;

public record RegisterLocalMemberCommand(
        String loginId,
        String password,
        String nickname,
        List<SubmitRequiredAgreementsCommand.AgreementConsentCommand> agreements
) {
}
