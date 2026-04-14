package matchuri.backend.domain.member.service;

import java.util.List;

public record RegisterLocalMemberCommand(
        String loginId,
        String password,
        String nickname,
        List<SubmitRequiredAgreementsCommand.AgreementConsentCommand> agreements
) {
}
