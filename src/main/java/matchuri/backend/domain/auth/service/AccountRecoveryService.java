package matchuri.backend.domain.auth.service;

import matchuri.backend.domain.auth.command.FindLoginIdCommand;
import matchuri.backend.domain.auth.command.ResetPasswordCommand;
import matchuri.backend.domain.auth.result.FindLoginIdResult;
import matchuri.backend.domain.auth.result.ResetPasswordResult;

public interface AccountRecoveryService {

    FindLoginIdResult findLoginId(FindLoginIdCommand command);

    ResetPasswordResult resetPassword(ResetPasswordCommand command);
}
