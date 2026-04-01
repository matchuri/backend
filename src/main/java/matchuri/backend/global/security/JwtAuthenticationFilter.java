package matchuri.backend.global.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.service.JwtTokenProvider;
import matchuri.backend.domain.member.entity.MemberRole;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_CODE_ATTRIBUTE = "matchuri.auth.error-code";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            request.setAttribute(AUTH_ERROR_CODE_ATTRIBUTE, AuthErrorCode.TOKEN_INVALID);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtTokenProvider.JwtClaims claims = jwtTokenProvider.parseAccessToken(
                    authorizationHeader.substring(BEARER_PREFIX.length())
            );
            AuthenticatedMember principal = new AuthenticatedMember(
                    claims.memberId(),
                    claims.loginId(),
                    MemberRole.valueOf(claims.role())
            );
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException exception) {
            request.setAttribute(AUTH_ERROR_CODE_ATTRIBUTE, AuthErrorCode.TOKEN_EXPIRED);
            SecurityContextHolder.clearContext();
        } catch (JwtException | IllegalArgumentException exception) {
            request.setAttribute(AUTH_ERROR_CODE_ATTRIBUTE, AuthErrorCode.TOKEN_INVALID);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
