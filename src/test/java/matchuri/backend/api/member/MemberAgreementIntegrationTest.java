package matchuri.backend.api.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.auth.service.CaptchaPurpose;
import matchuri.backend.domain.auth.service.CaptchaVerifier;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.image.entity.PresetProfileImage;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.image.repository.PresetProfileImageRepository;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberProfileImageRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberAgreementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileImageRepository memberProfileImageRepository;

    @Autowired
    private PresetProfileImageRepository presetProfileImageRepository;

    @Autowired
    private ImageAssetRepository imageAssetRepository;

    @Autowired
    private MemberAgreementRepository memberAgreementRepository;

    @Autowired
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private AuthExchangeCodeRepository authExchangeCodeRepository;

    @MockitoBean
    private CaptchaVerifier captchaVerifier;

    @BeforeEach
    void setUp() {
        given(captchaVerifier.verify(anyString(), eq(CaptchaPurpose.LOGIN), anyString())).willReturn(true);
        authExchangeCodeRepository.deleteAll();
        authRefreshTokenRepository.deleteAll();
        memberAgreementRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        memberProfileImageRepository.deleteAll();
        memberRepository.deleteAll();
        presetProfileImageRepository.deleteAll();
        imageAssetRepository.deleteAll();
        createDefaultPresetProfileImage();
    }

    @AfterEach
    void cleanUpProfileImages() {
        memberProfileImageRepository.deleteAll();
        presetProfileImageRepository.deleteAll();
        imageAssetRepository.deleteAll();
    }

    private void createDefaultPresetProfileImage() {
        ImageAsset asset = imageAssetRepository.save(new ImageAsset(
                ImageStorageProvider.CLOUDFLARE_R2,
                "test-bucket",
                "preset-profile/default.png",
                "default.png",
                "image/png",
                1024,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                320,
                320
        ));
        presetProfileImageRepository.save(new PresetProfileImage(asset, true));
    }

    @Test
    @DisplayName("미동의 회원은 필수 약관 상태를 조회할 수 있다")
    void getRequiredStatusWhenAgreementsMissing() throws Exception {
        createMemberThroughApi("agreement-user", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user", "P@ssw0rd!");

        mockMvc.perform(get("/api/v1/member-agreements/required-status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredAgreementsCompleted").value(false))
                .andExpect(jsonPath("$.data.missingAgreementTypes",
                        containsInAnyOrder("TERMS_OF_SERVICE", "PRIVACY_POLICY")));
    }

    @Test
    @DisplayName("필수 약관 동의 제출 후 완료 상태를 반환하고 핵심 API 접근이 가능해진다")
    void submitRequiredAgreementsAndAllowProtectedApi() throws Exception {
        createMemberThroughApi("agreement-user-2", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user-2", "P@ssw0rd!");

        MvcResult result = mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAgreementRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredAgreementsCompleted").value(true))
                .andExpect(jsonPath("$.data.missingAgreementTypes").isEmpty())
                .andExpect(jsonPath("$.data.onboarding.requiredAgreementsCompleted").value(true))
                .andExpect(jsonPath("$.data.onboarding.nextStep").exists())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.expiresIn").isNumber())
                .andReturn();

        assertThat(memberAgreementRepository.count()).isEqualTo(2);

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String refreshedAccessToken = body.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(refreshedAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("같은 타입과 버전으로 중복 제출해도 멱등하게 성공한다")
    void submitRequiredAgreementsIsIdempotent() throws Exception {
        createMemberThroughApi("agreement-user-3", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user-3", "P@ssw0rd!");

        mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAgreementRequest()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAgreementRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredAgreementsCompleted").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString());

        assertThat(memberAgreementRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("필수 약관 타입이 누락되면 MEMBER_AGREEMENT_REQUIRED_TYPES_MISSING을 반환한다")
    void submitRequiredAgreementsFailsWhenRequiredTypeMissing() throws Exception {
        createMemberThroughApi("agreement-user-4", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user-4", "P@ssw0rd!");

        mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agreements": [
                                    {
                                      "agreementType": "TERMS_OF_SERVICE",
                                      "agreementVersion": "2026-04-10"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_REQUIRED_TYPES_MISSING"));
    }

    @Test
    @DisplayName("잘못된 약관 타입은 MEMBER_AGREEMENT_INVALID_TYPE을 반환한다")
    void submitRequiredAgreementsFailsWhenTypeInvalid() throws Exception {
        createMemberThroughApi("agreement-user-5", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user-5", "P@ssw0rd!");

        mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agreements": [
                                    {
                                      "agreementType": "INVALID_TYPE",
                                      "agreementVersion": "2026-04-10"
                                    },
                                    {
                                      "agreementType": "PRIVACY_POLICY",
                                      "agreementVersion": "2026-04-10"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_INVALID_TYPE"));
    }

    @Test
    @DisplayName("버전이 다르면 MEMBER_AGREEMENT_VERSION_MISMATCH를 반환한다")
    void submitRequiredAgreementsFailsWhenVersionMismatch() throws Exception {
        createMemberThroughApi("agreement-user-6", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user-6", "P@ssw0rd!");

        mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agreements": [
                                    {
                                      "agreementType": "TERMS_OF_SERVICE",
                                      "agreementVersion": "2026-03-01"
                                    },
                                    {
                                      "agreementType": "PRIVACY_POLICY",
                                      "agreementVersion": "2026-04-10"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_VERSION_MISMATCH"));
    }

    @Test
    @DisplayName("미동의 회원은 핵심 API에서 MEMBER_AGREEMENT_REQUIRED로 차단되고 로그아웃은 허용된다")
    void blockProtectedApiBeforeAgreementCompletion() throws Exception {
        createMemberThroughApi("agreement-user-7", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user-7", "P@ssw0rd!");

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_REQUIRED"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .cookie(authSession.refreshTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loggedOut").value(true));
    }

    @Test
    @DisplayName("과거 버전과 최신 버전 이력이 함께 있어도 상태 조회와 재발급 token은 최신 버전 기준으로 완료 상태를 유지한다")
    void requiredStatusAndRefreshedTokenUseCurrentVersionExistenceCheck() throws Exception {
        createMemberThroughApi("agreement-user-8", "P@ssw0rd!");
        AuthSession authSession = login("agreement-user-8", "P@ssw0rd!");

        MvcResult consentResult = mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAgreementRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredAgreementsCompleted").value(true))
                .andReturn();

        JsonNode consentBody = objectMapper.readTree(consentResult.getResponse().getContentAsString());
        String refreshedAccessToken = consentBody.path("data").path("accessToken").asText();

        Member member = memberRepository.findByLoginId("agreement-user-8").orElseThrow();
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.TERMS_OF_SERVICE, "2026-03-01"));
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.PRIVACY_POLICY, "2026-03-01"));

        mockMvc.perform(get("/api/v1/member-agreements/required-status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(refreshedAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requiredAgreementsCompleted").value(true))
                .andExpect(jsonPath("$.data.missingAgreementTypes").isEmpty());

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(refreshedAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    private void createMemberThroughApi(String loginId, String password) throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "%s",
                                  "password": "%s"
                                }
                                """.formatted(loginId, password)))
                .andExpect(status().isOk());
    }

    private AuthSession login(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "%s",
                                  "password": "%s",
                                  "captchaToken": "test-captcha-token"
                                }
                                """.formatted(loginId, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new AuthSession(
                body.path("data").path("accessToken").asText(),
                result.getResponse().getCookie("matchuri_refresh_token")
        );
    }

    private String validAgreementRequest() {
        return """
                {
                  "agreements": [
                    {
                      "agreementType": "TERMS_OF_SERVICE",
                      "agreementVersion": "2026-04-10"
                    },
                    {
                      "agreementType": "PRIVACY_POLICY",
                      "agreementVersion": "2026-04-10"
                    }
                  ]
                }
                """;
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record AuthSession(
            String accessToken,
            Cookie refreshTokenCookie
    ) {
    }
}
