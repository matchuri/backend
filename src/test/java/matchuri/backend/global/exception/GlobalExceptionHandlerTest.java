package matchuri.backend.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import matchuri.backend.domain.common.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("요청 바디 검증 실패는 공통 오류 응답 형식으로 반환한다")
    void returnsValidationErrorResponseForInvalidBody() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.INVALID_BODY_FIELD.getCode()))
                .andExpect(jsonPath("$.error.details[0].source").value("BODY"))
                .andExpect(jsonPath("$.error.details[0].field").value("name"));
    }

    @Test
    @DisplayName("경로 변수 타입 불일치는 PATH 상세 정보와 함께 반환한다")
    void returnsPathValidationErrorResponseForTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/path/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.INVALID_PATH_VARIABLE.getCode()))
                .andExpect(jsonPath("$.error.details[0].source").value("PATH"))
                .andExpect(jsonPath("$.error.details[0].field").value("id"));
    }

    @Test
    @DisplayName("비즈니스 예외는 공통 오류 응답 형식으로 반환한다")
    void returnsBusinessErrorResponse() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.INVALID_REQUEST.getCode()))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    @DisplayName("예상하지 못한 예외는 내부 서버 오류 응답으로 변환한다")
    void returnsInternalServerErrorResponse() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    @DisplayName("존재하지 않는 경로는 리소스 없음 응답으로 변환한다")
    void returnsNotFoundResponseForMissingResource() throws Exception {
        mockMvc.perform(get("/__missing__/resource.txt"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.RESOURCE_NOT_FOUND.getCode()));
    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @PostMapping("/body")
        void validateBody(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/path/{id}")
        String validatePath(@PathVariable Long id) {
            return id.toString();
        }

        @GetMapping("/business")
        void throwBusinessException() {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        @GetMapping("/unexpected")
        void throwUnexpectedException() {
            throw new IllegalStateException("boom");
        }
    }

    record TestRequest(
            @NotBlank(message = "name은 비어 있을 수 없습니다.")
            String name
    ) {
    }
}
