package matchuri.backend.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import matchuri.backend.domain.auth.support.token.JwtTokenProvider;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "local"})
class AuthTestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("로컬 테스트 로그인은 활성 회원의 Access Token만 발급한다")
    void issuesAccessTokenForActiveMember() throws Exception {
        Member member = memberRepository.save(member("load-user", MemberStatus.ACTIVE));

        MvcResult result = mockMvc.perform(post("/api/test/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberId": %d
                                }
                                """.formatted(member.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andExpect(jsonPath("$.data.member.id").value(member.getId()))
                .andExpect(jsonPath("$.data.member.role").value("MEMBER"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = body.path("data").path("accessToken").asText();
        JwtTokenProvider.JwtClaims claims = jwtTokenProvider.parseAccessToken(accessToken);

        assertThat(claims.memberId()).isEqualTo(member.getId());
        assertThat(claims.loginId()).isEqualTo("load-user");
        assertThat(claims.role()).isEqualTo("MEMBER");
    }

    @Test
    @DisplayName("로컬 테스트 로그인은 존재하지 않는 회원을 404로 거절한다")
    void rejectsUnknownMember() throws Exception {
        mockMvc.perform(post("/api/test/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberId": 999999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("MEMBER_NOT_FOUND"));
    }

    @Test
    @DisplayName("로컬 테스트 로그인은 비활성 회원을 403으로 거절한다")
    void rejectsInactiveMember() throws Exception {
        Member member = memberRepository.save(member("inactive-load-user", MemberStatus.DELETED));

        mockMvc.perform(post("/api/test/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberId": %d
                                }
                                """.formatted(member.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INACTIVE_MEMBER"));
    }

    @Test
    @DisplayName("로컬 테스트 로그인은 유효하지 않은 memberId를 400으로 거절한다")
    void rejectsInvalidMemberId() throws Exception {
        mockMvc.perform(post("/api/test/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_BODY_FIELD"))
                .andExpect(jsonPath("$.error.details[0].field").value("memberId"));
    }

    private Member member(String loginId, MemberStatus status) {
        return Member.builder()
                .loginId(loginId)
                .passwordHash("unused-password-hash")
                .nickname(loginId)
                .nicknameCompleted(true)
                .email(loginId + "@example.com")
                .social(false)
                .memberRole(MemberRole.MEMBER)
                .status(status)
                .build();
    }
}
