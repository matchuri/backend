package matchuri.backend.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.MemberAgreementErrorCode;
import matchuri.backend.domain.member.service.RequiredAgreementVersions;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RequiredAgreementAccessFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_ERROR_CODE_ATTRIBUTE = "matchuri.authorization.error-code";

    private final MatchuriProperties matchuriProperties;
    private final MatchuriAccessDeniedHandler accessDeniedHandler;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedMember authenticatedMember)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!RequiredAgreementVersions.currentRevision().equals(authenticatedMember.requiredAgreementRevision())) {
            request.setAttribute(AUTHORIZATION_ERROR_CODE_ATTRIBUTE, MemberAgreementErrorCode.REQUIRED);
            accessDeniedHandler.handle(request, response, new AccessDeniedException(MemberAgreementErrorCode.REQUIRED.getMessage()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        return buildAllowedMatchers().stream().anyMatch(matcher -> matcher.matches(request, antPathMatcher));
    }

    private List<AllowedRequest> buildAllowedMatchers() {
        List<AllowedRequest> matchers = new ArrayList<>();
        MatchuriProperties.Auth auth = matchuriProperties.getAuth();

        auth.getPublicApiPatterns().forEach(pattern -> matchers.add(new AllowedRequest(null, pattern)));
        auth.getPublicGetApiPatterns().forEach(pattern -> matchers.add(new AllowedRequest(HttpMethod.GET, pattern)));
        auth.getPublicPostApiPatterns().forEach(pattern -> matchers.add(new AllowedRequest(HttpMethod.POST, pattern)));
        auth.getPublicOptionsApiPatterns().forEach(pattern -> matchers.add(new AllowedRequest(HttpMethod.OPTIONS, pattern)));

        matchers.add(new AllowedRequest(HttpMethod.POST, "/api/v1/auth/logout"));
        matchers.add(new AllowedRequest(HttpMethod.GET, "/api/v1/member-agreements/required-status"));
        matchers.add(new AllowedRequest(HttpMethod.POST, "/api/v1/member-agreements/consents"));
        return matchers;
    }

    private record AllowedRequest(HttpMethod method, String pattern) {

        private boolean matches(HttpServletRequest request, AntPathMatcher antPathMatcher) {
            boolean methodMatches = method == null || method.matches(request.getMethod());
            return methodMatches && antPathMatcher.match(pattern, request.getRequestURI());
        }
    }
}
