package matchuri.backend.api.member;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.global.docs.RestDocsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
class MemberControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private AuthExchangeCodeRepository authExchangeCodeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        authExchangeCodeRepository.deleteAll();
        authRefreshTokenRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        memberRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("이미 존재하는 loginId는 exists=true로 반환한다")
    void returnsTrueWhenLoginIdExists() throws Exception {
        memberRepository.save(new Member(
                "tester01",
                "hashed-password",
                "tester01@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(get("/api/v1/members/exists/{loginId}", "tester01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("tester01"))
                .andExpect(jsonPath("$.data.exists").value(true))
                .andDo(document("members/check-login-id",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("loginId").description(
                                        """
                                        회원 가입 시 사용할 로그인 ID입니다.
                                        필수 path variable입니다.
                                        제약:
                                        - 1자 이상 50자 이하
                                        - 공백 불가
                                        - 허용 문자: 영문 대소문자, 숫자, 점(.), 밑줄(_), 하이픈(-)
                                        - 서버 정규식 검증: ^[A-Za-z0-9._-]+$
                                        허용 예시: tester01, matchuri-user, user.name
                                        비허용 예시: test user, 한글아이디, 50자 초과 문자열
                                        """
                                )
                        ),
                        RestDocsSupport.successResponse(
                                fieldWithPath("data.loginId")
                                        .description("중복 확인한 로그인 ID입니다. 요청 path variable과 동일한 값을 반환합니다."),
                                fieldWithPath("data.exists")
                                        .description("이미 존재하는 로그인 ID인지 여부입니다. true면 이미 사용 중이고, false면 회원 가입에 사용할 수 있습니다.")
                        )));
    }

    @Test
    @DisplayName("존재하지 않는 loginId는 exists=false로 반환한다")
    void returnsFalseWhenLoginIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/members/exists/{loginId}", "new-user")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("new-user"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @DisplayName("형식이 잘못된 loginId는 공통 경로 변수 오류 응답을 반환한다")
    void returnsPathValidationErrorForInvalidLoginId() throws Exception {
        mockMvc.perform(get("/api/v1/members/exists/{loginId}", "invalid login id")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_PATH_VARIABLE"))
                .andExpect(jsonPath("$.error.details[0].source").value("PATH"))
                .andExpect(jsonPath("$.error.details[0].field").value("loginId"))
                .andDo(document("members/check-login-id-invalid",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("loginId").description(
                                        """
                                        형식 검증 대상 로그인 ID입니다.
                                        대표 실패 케이스:
                                        - 공백 포함
                                        - 허용되지 않은 문자 포함
                                        - 50자 초과
                                        """
                                )
                        ),
                        RestDocsSupport.errorResponse()));
    }
}
