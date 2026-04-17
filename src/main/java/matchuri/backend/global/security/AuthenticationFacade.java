package matchuri.backend.global.security;

import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.global.exception.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

    public AuthenticatedMember getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedMember authenticatedMember)) {
            throw new AuthenticationException(AuthErrorCode.TOKEN_MISSING);
        }

        return authenticatedMember;
    }
}
