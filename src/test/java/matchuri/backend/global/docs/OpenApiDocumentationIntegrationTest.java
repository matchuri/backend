package matchuri.backend.global.docs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OpenAPI 문서에 loginId 중복 확인 API의 요약과 제약 설명이 노출된다")
    void exposesFriendlyMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.summary")
                        .value("로그인 ID 중복 확인"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.description")
                        .value(org.hamcrest.Matchers.containsString("인증 없이 호출할 수 있습니다.")))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.parameters[0].description")
                        .value(org.hamcrest.Matchers.containsString("1자 이상 50자 이하")))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.parameters[0].example")
                        .value("tester01"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.security").isEmpty())
                .andExpect(jsonPath("$.components.schemas.LoginIdExistsResponse.properties.loginId.description")
                        .value("중복 확인한 로그인 ID"))
                .andExpect(jsonPath("$.components.schemas.LoginIdExistsResponse.properties.exists.description")
                        .value(org.hamcrest.Matchers.containsString("이미 존재하는 로그인 ID인지 여부")));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Auth 공개 API의 비인증 정책과 envelope 응답 스키마가 노출된다")
    void exposesAuthPublicApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.summary")
                        .value("로컬 로그인"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/refresh'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/refresh'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/oauth2/google'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/oauth2/exchange'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/oauth2/exchange'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/logout'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LogoutApiResponse"));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Member 공개 API의 비인증 정책과 envelope 응답 스키마가 노출된다")
    void exposesMemberPublicApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/members/signup'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/members/signup'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/RegisterLocalMemberApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/members'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/members'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/CreateMemberApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginIdExistsApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/nickname/{nickname}'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/nickname/{nickname}'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/NicknameExistsApiResponse"));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Member Agreement API의 envelope 응답 스키마가 노출된다")
    void exposesMemberAgreementApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/member-agreements/required-status'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/RequiredAgreementStatusApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/member-agreements/consents'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/SubmitRequiredAgreementsApiResponse"))
                .andExpect(jsonPath("$.components.schemas.SubmitRequiredAgreementsRequest.properties.agreements.description")
                        .value(org.hamcrest.Matchers.containsString("필수 약관 동의 목록")))
                .andExpect(jsonPath("$.components.schemas.AgreementConsentRequest.properties.agreementType.description")
                        .value("약관 종류입니다."))
                .andExpect(jsonPath("$.components.schemas.SubmitRequiredAgreementsResponse.properties.accessToken.description")
                        .value(org.hamcrest.Matchers.containsString("새 access token")));
    }
}
