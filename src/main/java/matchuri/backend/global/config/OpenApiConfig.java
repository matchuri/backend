package matchuri.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final Pattern API_ID_PREFIX_PATTERN = Pattern.compile("^\\[[A-Z0-9]+\\.\\d{3}\\.\\d{3}]\\s*");
    private static final List<Tag> API_FLOW_TAGS = List.of(
            tag("00 Ops", "공통/운영 API"),
            tag("01 Auth", "인증/가입 플로우 API"),
            tag("02 Onboarding", "온보딩/내 정보 플로우 API"),
            tag("03 Menu Reference", "메뉴/취향 참조 플로우 API"),
            tag("04 Recommendation", "개인 추천 플로우 API"),
            tag("05 Group", "그룹 생성/참여 플로우 API"),
            tag("06 Group Recommendation", "그룹 추천/투표 플로우 API"),
            tag("09 Admin", "관리자 데이터 운영 API"));
    private static final Map<ApiOperationKey, ApiOperationMetadata> API_OPERATION_METADATA = apiOperationMetadata();

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
                                .tags(List.of("00 Ops"))
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

    @Bean
    public OpenApiCustomizer apiFlowMetadataCustomizer() {
        return openApi -> {
            openApi.setTags(API_FLOW_TAGS);
            API_OPERATION_METADATA.forEach((key, metadata) -> applyApiMetadata(openApi, key, metadata));
            sortPathsByApiOperationMetadata(openApi);
        };
    }

    private static void applyApiMetadata(OpenAPI openApi, ApiOperationKey key, ApiOperationMetadata metadata) {
        Paths paths = openApi.getPaths();
        if (paths == null || !paths.containsKey(key.path())) {
            return;
        }

        Operation operation = paths.get(key.path()).readOperationsMap().get(key.method());
        if (operation == null) {
            return;
        }

        operation.setTags(List.of(metadata.tagName()));
        operation.addExtension("x-api-id", metadata.apiId());
        operation.setSummary(withApiIdPrefix(metadata.apiId(), operation.getSummary()));
    }

    private static void sortPathsByApiOperationMetadata(OpenAPI openApi) {
        Paths paths = openApi.getPaths();
        if (paths == null) {
            return;
        }

        Paths sortedPaths = new Paths();
        API_OPERATION_METADATA.keySet().forEach(key -> {
            if (paths.containsKey(key.path()) && !sortedPaths.containsKey(key.path())) {
                sortedPaths.addPathItem(key.path(), paths.get(key.path()));
            }
        });
        paths.forEach((path, pathItem) -> {
            if (!sortedPaths.containsKey(path)) {
                sortedPaths.addPathItem(path, pathItem);
            }
        });

        openApi.setPaths(sortedPaths);
    }

    private static String withApiIdPrefix(String apiId, String summary) {
        String normalizedSummary = summary == null ? "" : API_ID_PREFIX_PATTERN.matcher(summary).replaceFirst("");
        return "[" + apiId + "] " + normalizedSummary;
    }

    private static Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }

    private static Map<ApiOperationKey, ApiOperationMetadata> apiOperationMetadata() {
        Map<ApiOperationKey, ApiOperationMetadata> metadata = new LinkedHashMap<>();

        metadata.put(key("/api/v1/health", PathItem.HttpMethod.GET), meta("OPS.010.000", "00 Ops"));

        metadata.put(key("/api/v1/auth/email", PathItem.HttpMethod.POST), meta("AUTH.010.000", "01 Auth"));
        metadata.put(key("/api/v1/auth/email/confirm", PathItem.HttpMethod.POST), meta("AUTH.020.000", "01 Auth"));
        metadata.put(key("/api/v1/members/exists/{loginId}", PathItem.HttpMethod.GET), meta("AUTH.030.000", "01 Auth"));
        metadata.put(
                key("/api/v1/members/exists/nickname/{nickname}", PathItem.HttpMethod.GET),
                meta("AUTH.040.000", "01 Auth"));
        metadata.put(key("/api/v1/members/signup", PathItem.HttpMethod.POST), meta("AUTH.050.000", "01 Auth"));
        metadata.put(key("/api/v1/auth/oauth2/{provider}", PathItem.HttpMethod.GET), meta("AUTH.060.000", "01 Auth"));
        metadata.put(
                key("/api/v1/auth/oauth2/exchange", PathItem.HttpMethod.POST),
                meta("AUTH.070.000", "01 Auth"));
        metadata.put(key("/api/v1/auth/login", PathItem.HttpMethod.POST), meta("AUTH.080.000", "01 Auth"));
        metadata.put(key("/api/v1/auth/refresh", PathItem.HttpMethod.POST), meta("AUTH.090.000", "01 Auth"));
        metadata.put(key("/api/v1/auth/logout", PathItem.HttpMethod.POST), meta("AUTH.100.000", "01 Auth"));
        metadata.put(
                key("/api/v1/auth/recovery/login-id", PathItem.HttpMethod.POST),
                meta("AUTH.110.000", "01 Auth"));
        metadata.put(
                key("/api/v1/auth/recovery/password", PathItem.HttpMethod.POST),
                meta("AUTH.120.000", "01 Auth"));
        metadata.put(key("/api/v1/members", PathItem.HttpMethod.POST), meta("AUTH.900.000", "01 Auth"));

        metadata.put(
                key("/api/v1/member-agreements/required-status", PathItem.HttpMethod.GET),
                meta("ONB.010.000", "02 Onboarding"));
        metadata.put(
                key("/api/v1/member-agreements/consents", PathItem.HttpMethod.POST),
                meta("ONB.020.000", "02 Onboarding"));
        metadata.put(key("/api/v1/members/me", PathItem.HttpMethod.GET), meta("ONB.030.000", "02 Onboarding"));
        metadata.put(key("/api/v1/members/me", PathItem.HttpMethod.PATCH), meta("ONB.040.000", "02 Onboarding"));
        metadata.put(
                key("/api/v1/members/me/password", PathItem.HttpMethod.PATCH),
                meta("ONB.050.000", "02 Onboarding"));
        metadata.put(
                key("/api/v1/members/me/taste-profile", PathItem.HttpMethod.GET),
                meta("ONB.060.000", "02 Onboarding"));
        metadata.put(
                key("/api/v1/members/me/taste-profile", PathItem.HttpMethod.PATCH),
                meta("ONB.070.000", "02 Onboarding"));
        metadata.put(key("/api/v1/members/me", PathItem.HttpMethod.DELETE), meta("ONB.080.000", "02 Onboarding"));

        metadata.put(
                key("/api/v1/attribute-categories", PathItem.HttpMethod.GET),
                meta("REF.010.000", "03 Menu Reference"));
        metadata.put(
                key("/api/v1/restriction-ingredients", PathItem.HttpMethod.GET),
                meta("REF.020.000", "03 Menu Reference"));
        metadata.put(key("/api/v1/menu-items", PathItem.HttpMethod.GET), meta("REF.030.000", "03 Menu Reference"));
        metadata.put(
                key("/api/v1/menu-items/{menuItemId}", PathItem.HttpMethod.GET),
                meta("REF.040.000", "03 Menu Reference"));

        metadata.put(
                key("/api/v1/guest/recommendations", PathItem.HttpMethod.POST),
                meta("REC.010.000", "04 Recommendation"));
        metadata.put(
                key("/api/v1/personal/recommendations", PathItem.HttpMethod.GET),
                meta("REC.020.000", "04 Recommendation"));
        metadata.put(
                key("/api/v1/personal/recommendations", PathItem.HttpMethod.POST),
                meta("REC.030.000", "04 Recommendation"));
        metadata.put(
                key("/api/v1/personal/recommendations/{requestId}/reroll", PathItem.HttpMethod.POST),
                meta("REC.040.000", "04 Recommendation"));
        metadata.put(
                key("/api/v1/personal/recommendations/{requestId}", PathItem.HttpMethod.GET),
                meta("REC.050.000", "04 Recommendation"));
        metadata.put(
                key("/api/v1/personal/recommendations/{requestId}/candidates", PathItem.HttpMethod.GET),
                meta("REC.060.000", "04 Recommendation"));
        metadata.put(
                key("/api/v1/personal/recommendations/{requestId}", PathItem.HttpMethod.PATCH),
                meta("REC.070.000", "04 Recommendation"));

        metadata.put(key("/api/v1/groups", PathItem.HttpMethod.POST), meta("GROUP.010.000", "05 Group"));
        metadata.put(key("/api/v1/groups", PathItem.HttpMethod.GET), meta("GROUP.020.000", "05 Group"));
        metadata.put(key("/api/v1/groups/{groupId}", PathItem.HttpMethod.GET), meta("GROUP.030.000", "05 Group"));
        metadata.put(
                key("/api/v1/groups/{groupId}", PathItem.HttpMethod.PATCH),
                meta("GROUP.040.000", "05 Group"));
        metadata.put(
                key("/api/v1/groups/invites/nickname", PathItem.HttpMethod.POST),
                meta("GROUP.050.000", "05 Group"));
        metadata.put(
                key("/api/v1/groups/invites/me", PathItem.HttpMethod.GET),
                meta("GROUP.060.000", "05 Group"));
        metadata.put(
                key("/api/v1/groups/invites/{inviteId}/response", PathItem.HttpMethod.POST),
                meta("GROUP.070.000", "05 Group"));
        metadata.put(key("/api/v1/groups/join", PathItem.HttpMethod.POST), meta("GROUP.080.000", "05 Group"));
        metadata.put(
                key("/api/v1/groups/{groupId}/leave", PathItem.HttpMethod.POST),
                meta("GROUP.090.000", "05 Group"));
        metadata.put(key("/api/v1/groups/{groupId}", PathItem.HttpMethod.DELETE), meta("GROUP.100.000", "05 Group"));

        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations", PathItem.HttpMethod.POST),
                meta("GREC.010.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations/{sessionId}/readiness", PathItem.HttpMethod.GET),
                meta("GREC.020.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready", PathItem.HttpMethod.POST),
                meta("GREC.030.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations", PathItem.HttpMethod.GET),
                meta("GREC.040.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations/{sessionId}", PathItem.HttpMethod.GET),
                meta("GREC.050.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations/{sessionId}/candidates", PathItem.HttpMethod.GET),
                meta("GREC.060.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations/{sessionId}/reroll", PathItem.HttpMethod.POST),
                meta("GREC.070.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes", PathItem.HttpMethod.POST),
                meta("GREC.080.000", "06 Group Recommendation"));
        metadata.put(
                key("/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize", PathItem.HttpMethod.PATCH),
                meta("GREC.090.000", "06 Group Recommendation"));

        metadata.put(
                key("/api/v1/admin/attribute-categories", PathItem.HttpMethod.GET),
                meta("ADMIN.010.000", "09 Admin"));
        metadata.put(
                key("/api/v1/admin/attribute-categories", PathItem.HttpMethod.POST),
                meta("ADMIN.020.000", "09 Admin"));
        metadata.put(
                key("/api/v1/admin/attribute-categories/{attributeCategoryId}", PathItem.HttpMethod.PATCH),
                meta("ADMIN.030.000", "09 Admin"));
        metadata.put(
                key("/api/v1/admin/attribute-categories/{attributeCategoryId}", PathItem.HttpMethod.DELETE),
                meta("ADMIN.040.000", "09 Admin"));
        metadata.put(key("/api/v1/admin/ingredients", PathItem.HttpMethod.GET), meta("ADMIN.050.000", "09 Admin"));
        metadata.put(key("/api/v1/admin/ingredients", PathItem.HttpMethod.POST), meta("ADMIN.060.000", "09 Admin"));
        metadata.put(
                key("/api/v1/admin/ingredients/{ingredientId}", PathItem.HttpMethod.PATCH),
                meta("ADMIN.070.000", "09 Admin"));
        metadata.put(
                key("/api/v1/admin/ingredients/{ingredientId}", PathItem.HttpMethod.DELETE),
                meta("ADMIN.080.000", "09 Admin"));
        metadata.put(key("/api/v1/admin/menu-items", PathItem.HttpMethod.GET), meta("ADMIN.090.000", "09 Admin"));
        metadata.put(
                key("/api/v1/admin/menu-items/{menuItemId}", PathItem.HttpMethod.PATCH),
                meta("ADMIN.100.000", "09 Admin"));
        metadata.put(
                key("/api/v1/admin/menu-items/{menuItemId}", PathItem.HttpMethod.DELETE),
                meta("ADMIN.110.000", "09 Admin"));

        return metadata;
    }

    private static ApiOperationKey key(String path, PathItem.HttpMethod method) {
        return new ApiOperationKey(path, method);
    }

    private static ApiOperationMetadata meta(String apiId, String tagName) {
        return new ApiOperationMetadata(apiId, tagName);
    }

    private record ApiOperationKey(String path, PathItem.HttpMethod method) {
    }

    private record ApiOperationMetadata(String apiId, String tagName) {
    }
}
