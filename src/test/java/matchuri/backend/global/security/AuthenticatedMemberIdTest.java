package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import matchuri.backend.domain.member.entity.MemberRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.context.request.ServletWebRequest;

class AuthenticatedMemberIdTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 principal에서 memberId만 컨트롤러 인자로 추출한다")
    void resolvesMemberIdFromAuthenticatedMember() throws Exception {
        AuthenticatedMember principal = new AuthenticatedMember(
                42L,
                "member42",
                MemberRole.MEMBER,
                "2026-04-10"
        );
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of())
        );

        Method method = TestController.class.getDeclaredMethod("handle", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        AuthenticationPrincipalArgumentResolver resolver = new AuthenticationPrincipalArgumentResolver();

        assertThat(resolver.supportsParameter(parameter)).isTrue();
        assertThat(resolver.resolveArgument(
                parameter,
                null,
                new ServletWebRequest(new MockHttpServletRequest()),
                null
        )).isEqualTo(42L);
    }

    private static final class TestController {
        void handle(@AuthenticatedMemberId Long memberId) {
        }
    }
}
