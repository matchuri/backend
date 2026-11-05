package matchuri.backend.global.security;

import io.swagger.v3.oas.annotations.Parameter;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 비회원과 로그인 회원을 모두 허용하는 API에서 인증 회원의 식별자를 선택적으로 주입한다.
 *
 * <p>principal이 {@link AuthenticatedMember}이면 {@code memberId}를 반환하고, 인증 정보가 없거나
 * 익명 또는 다른 타입의 principal이면 {@code null}을 반환한다. 따라서 적용 대상 매개변수는
 * primitive {@code long}이 아닌 nullable {@link Long}으로 선언해야 한다.
 *
 * <p>이 어노테이션은 principal에서 식별자를 추출하는 역할만 담당한다. 만료되거나 유효하지 않은
 * 토큰을 거절할지는 JWT 인증 필터와 Spring Security 접근 정책에서 별도로 결정해야 한다.
 * 인증이 반드시 필요한 API에서는 {@link AuthenticatedMemberId}를 사용한다.
 *
 * <p>인증 정보는 HTTP 요청값이 아니므로 Swagger/OpenAPI 요청 파라미터에는 노출하지 않는다.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
@AuthenticationPrincipal(        expression = """
                #this instanceof T(matchuri.backend.global.security.AuthenticatedMember)
                ? #this.memberId
                : null
                """
)
public @interface OptionalAuthenticatedMemberId {
}
