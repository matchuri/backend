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
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.security").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.LoginIdExistsResponse.properties.loginId.description")
                        .value("중복 확인한 로그인 ID"))
                .andExpect(jsonPath("$.components.schemas.LoginIdExistsResponse.properties.exists.description")
                        .value(org.hamcrest.Matchers.containsString("이미 존재하는 로그인 ID인지 여부")));
    }
}
