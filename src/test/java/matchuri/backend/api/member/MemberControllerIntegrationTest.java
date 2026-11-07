package matchuri.backend.api.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private AuthExchangeCodeRepository authExchangeCodeRepository;

    @BeforeEach
    void setUp() {
        authExchangeCodeRepository.deleteAll();
        authRefreshTokenRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        memberRepository.deleteAll();
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
                .andExpect(jsonPath("$.data.exists").value(true));
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
    @DisplayName("이미 존재하는 nickname은 exists=true로 반환한다")
    void returnsTrueWhenNicknameExists() throws Exception {
        Member member = Member.builder()
                .loginId("tester01")
                .passwordHash("hashed-password")
                .nickname("example_google")
                .email("tester01@example.com")
                .social(false)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(member);

        mockMvc.perform(get("/api/v1/members/exists/nickname/{nickname}", "example_google")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("example_google"))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 nickname은 exists=false로 반환한다")
    void returnsFalseWhenNicknameDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/members/exists/nickname/{nickname}", "example_google")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("example_google"))
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
                .andExpect(jsonPath("$.error.details[0].field").value("loginId"));
    }

    @Test
    @DisplayName("형식이 잘못된 nickname은 공통 경로 변수 오류 응답을 반환한다")
    void returnsPathValidationErrorForInvalidNickname() throws Exception {
        mockMvc.perform(get("/api/v1/members/exists/nickname/{nickname}", " ")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_PATH_VARIABLE"))
                .andExpect(jsonPath("$.error.details[0].source").value("PATH"))
                .andExpect(jsonPath("$.error.details[0].field").value("nickname"));
    }
}
