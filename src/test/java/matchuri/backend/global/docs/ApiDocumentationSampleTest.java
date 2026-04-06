package matchuri.backend.global.docs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.constraints.NotBlank;
import matchuri.backend.domain.common.CommonErrorCode;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.validation.annotation.Validated;

@WebMvcTest(
        controllers = ApiDocumentationSampleTest.SampleController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@Import({GlobalExceptionHandler.class, ApiDocumentationSampleTest.SampleController.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
class ApiDocumentationSampleTest {

    @MockitoBean
    private matchuri.backend.global.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("공통 성공 응답 문서 스니펫을 생성한다")
    void documentSuccessResponse() throws Exception {
        mockMvc.perform(get("/docs-sample/ping")
                        .header("Authorization", "Bearer sample-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("common/success-response",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        RestDocsSupport.bearerAuthorizationHeader(),
                        RestDocsSupport.successResponse(
                                fieldWithPath("data.message").description("응답 메시지")
                        )));
    }

    @Test
    @DisplayName("공통 에러 응답 문서 스니펫을 생성한다")
    void documentErrorResponse() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.get("/docs-sample/error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("common/error-response",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        RestDocsSupport.errorResponse()));
    }

    @Test
    @DisplayName("검증 실패 응답 문서 스니펫을 생성한다")
    void documentValidationErrorResponse() throws Exception {
        mockMvc.perform(get("/docs-sample/query")
                        .param("keyword", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(document("common/validation-error-response",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        RestDocsSupport.errorResponse()));
    }

    @RestController
    @RequestMapping("/docs-sample")
    @Validated
    static class SampleController {

        @GetMapping("/ping")
        ApiResponse<PingResponse> ping() {
            return ApiResponse.success(new PingResponse("pong"));
        }

        @GetMapping("/error")
        ApiResponse<Void> error() {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        @GetMapping("/query")
        ApiResponse<Void> query(
                @NotBlank(message = "keyword는 비어 있을 수 없습니다.")
                @RequestParam String keyword
        ) {
            return ApiResponse.successWithoutData();
        }
    }

    record PingResponse(String message) {
    }
}
