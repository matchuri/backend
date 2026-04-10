package matchuri.backend.global.config;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI matchuriOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Matchuri Backend API")
                        .version("v1")
                        .description("Matchuri MVP backend API documentation")
                        .contact(new Contact().name("Matchuri Team")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("인증이 필요한 API는 Authorization Bearer 토큰을 사용합니다.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .path("/api/v1/health", new PathItem().get(
                        new Operation()
                                .tags(List.of("Ops"))
                                .summary("헬스 체크")
                                .description("""
                                        백엔드 서비스의 기본 가용 상태를 확인합니다.

                                        - 인증 없이 호출할 수 있습니다.
                                        - 공개 API이므로 상세 내부 상태는 노출하지 않습니다.
                                        - 응답의 `status`가 `UP`이면 기본 서비스 상태는 정상입니다.
                                        """)
                                .security(List.of())
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse()
                                                .description("헬스 체크 성공")
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType()
                                                                .schema(new ObjectSchema())
                                                                .addExamples("success", new Example()
                                                                        .value("""
                                                                                {
                                                                                  "groups": [
                                                                                    "liveness",
                                                                                    "readiness"
                                                                                  ],
                                                                                  "status": "UP"
                                                                                }
                                                                                """))))))));
    }
}
