package matchuri.backend.global.security;

import io.swagger.v3.oas.annotations.Parameter;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 인증된 Matchuri 회원의 식별자를 Controller 메서드 매개변수로 주입한다.
 *
 * <p>{@link AuthenticatedMember} principal의 {@code memberId}를 추출하며, 인증이 필수인 API에서 사용한다.
 * 해당 API는 Spring Security 설정에서 인증된 요청만 Controller에 도달하도록 보장해야 한다.
 * 서비스 호출 시 주입받은 식별자는 command에 포함하지 않고 별도 인자로 전달한다.
 *
 * <p>인증 정보는 HTTP 요청값이 아니므로 Swagger/OpenAPI 요청 파라미터에는 노출하지 않는다.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
@AuthenticationPrincipal(expression = "memberId")
public @interface AuthenticatedMemberId {
}
