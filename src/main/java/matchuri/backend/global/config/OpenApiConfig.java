package matchuri.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
