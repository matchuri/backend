package matchuri.backend.api.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import matchuri.backend.domain.auth.entity.AuthExchangeCode;
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.auth.support.verification.EmailVerificationTokenGenerator;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileCategoryRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileDislikedMenuItemRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRestrictionIngredientRepository;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.global.config.MatchuriProperties;
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
@ActiveProfiles("test")
class MemberAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private AuthExchangeCodeRepository authExchangeCodeRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private EmailVerificationTokenGenerator emailVerificationTokenGenerator;

    @Autowired
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Autowired
    private MemberTasteProfileCategoryRepository memberTasteProfileCategoryRepository;

    @Autowired
    private MemberTasteProfileRestrictionIngredientRepository memberTasteProfileRestrictionIngredientRepository;

    @Autowired
    private MemberTasteProfileDislikedMenuItemRepository memberTasteProfileDislikedMenuItemRepository;

    @Autowired
    private MemberAgreementRepository memberAgreementRepository;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @BeforeEach
    void setUp() {
        emailVerificationRepository.deleteAll();
        authExchangeCodeRepository.deleteAll();
        authRefreshTokenRepository.deleteAll();
        memberAgreementRepository.deleteAll();
        memberTasteProfileCategoryRepository.deleteAll();
        memberTasteProfileRestrictionIngredientRepository.deleteAll();
        memberTasteProfileDislikedMenuItemRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
        menuItemRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("자체 회원가입 통합 API는 약관과 닉네임을 함께 저장하지만 로그인 상태는 만들지 않는다")
    void registerLocalMember() throws Exception {
        String emailVerificationToken = issueSignupEmailVerificationToken("signup@example.com");

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "signup-user",
                                  "password": "P@ssw0rd!",
                                  "nickname": "점심탐험가",
                                  "email": "signup@example.com",
                                  "emailVerificationToken": "%s",
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
                                """.formatted(emailVerificationToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("signup-user"))
                .andExpect(jsonPath("$.data.email").value("signup@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"))
                .andExpect(jsonPath("$.data.memberId").isNumber())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        Member member = memberRepository.findByLoginId("signup-user").orElseThrow();
        assertThat(member.getPasswordHash()).isNotEqualTo("P@ssw0rd!");
        assertThat(member.getEmail()).isEqualTo("signup@example.com");
        assertThat(member.getNickname()).isEqualTo("점심탐험가");
        assertThat(member.isNicknameCompleted()).isTrue();
        assertThat(memberAgreementRepository.count()).isEqualTo(2);
        assertThat(emailVerificationRepository.findByVerificationTokenHash(
                emailVerificationTokenGenerator.hashToken(emailVerificationToken)
        )).isPresent()
                .get()
                .extracting(EmailVerification::getVerificationTokenUsedAt)
                .isNotNull();

        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));

        AuthSession authSession = login("signup-user", "P@ssw0rd!");
        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value("signup-user"))
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"))
                .andExpect(jsonPath("$.data.isSocial").value(false))
                .andExpect(jsonPath("$.data.email").value("signup@example.com"));
    }

    @Test
    @DisplayName("자체 회원가입 통합 API에서 약관 검증이 실패하면 회원과 약관 기록이 남지 않는다")
    void registerLocalMemberRollsBackWhenAgreementValidationFails() throws Exception {
        String emailVerificationToken = issueSignupEmailVerificationToken("rollback@example.com");

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "rollback-user",
                                  "password": "P@ssw0rd!",
                                  "nickname": "롤백검증",
                                  "email": "rollback@example.com",
                                  "emailVerificationToken": "%s",
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
                                """.formatted(emailVerificationToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_VERSION_MISMATCH"));

        assertThat(memberRepository.findByLoginId("rollback-user")).isEmpty();
        assertThat(memberAgreementRepository.count()).isZero();
        assertThat(emailVerificationRepository.findByVerificationTokenHash(
                emailVerificationTokenGenerator.hashToken(emailVerificationToken)
        )).isPresent()
                .get()
                .extracting(EmailVerification::getVerificationTokenUsedAt)
                .isNull();
    }

    @Test
    @DisplayName("자체 회원가입 통합 API는 SIGNUP 이메일 인증 token이 유효하지 않으면 회원을 생성하지 않는다")
    void registerLocalMemberFailsWhenEmailVerificationTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "invalid-token-user",
                                  "password": "P@ssw0rd!",
                                  "nickname": "토큰검증",
                                  "email": "invalid-token@example.com",
                                  "emailVerificationToken": "ev_missing-token",
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
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_VERIFICATION_FAILED"));

        assertThat(memberRepository.findByLoginId("invalid-token-user")).isEmpty();
        assertThat(memberAgreementRepository.count()).isZero();
    }

    @Test
    @DisplayName("자체 회원가입 통합 API는 이미 자체 로그인 계정에 사용 중인 이메일이면 거절한다")
    void registerLocalMemberFailsWhenLocalEmailIsDuplicated() throws Exception {
        memberRepository.save(new Member(
                "existing-email-user",
                "hashed-password",
                "duplicate@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        String emailVerificationToken = issueSignupEmailVerificationToken("duplicate@example.com");

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "duplicate-email-user",
                                  "password": "P@ssw0rd!",
                                  "nickname": "이메일중복",
                                  "email": "duplicate@example.com",
                                  "emailVerificationToken": "%s",
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
                                """.formatted(emailVerificationToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_EMAIL"));

        assertThat(memberRepository.findByLoginId("duplicate-email-user")).isEmpty();
        assertThat(emailVerificationRepository.findByVerificationTokenHash(
                emailVerificationTokenGenerator.hashToken(emailVerificationToken)
        )).isPresent()
                .get()
                .extracting(EmailVerification::getVerificationTokenUsedAt)
                .isNull();
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호는 해시로 저장되고 생성 응답을 반환한다")
    void createMember() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "tester01",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("tester01"))
                .andExpect(jsonPath("$.data.memberId").isNumber());

        Member member = memberRepository.findByLoginId("tester01").orElseThrow();
        assertThat(member.getPasswordHash()).isNotEqualTo("P@ssw0rd!");
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("중복 로그인 아이디로 가입하면 MEMBER_DUPLICATE_LOGIN_ID를 반환한다")
    void createMemberWithDuplicateLoginId() throws Exception {
        memberRepository.save(new Member(
                "tester01",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "tester01",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_LOGIN_ID"));
    }

    @Test
    @DisplayName("로그인 상태 비밀번호 변경 API는 현재 세션을 유지한 채 비밀번호만 교체한다")
    void updateMyPasswordKeepsCurrentSession() throws Exception {
        createMemberThroughApi("password-user", "P@ssw0rd!");
        AuthSession authSession = login("password-user", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());
        String refreshToken = authSession.refreshTokenCookie().getValue();

        mockMvc.perform(patch("/api/v1/members/me/password")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "P@ssw0rd!",
                                  "newPassword": "N3wP@ssw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.passwordChanged").value(true))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        assertThat(authRefreshTokenRepository.findByToken(refreshToken)).isPresent();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(authSession.refreshTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "password-user",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_LOGIN_FAILED"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "password-user",
                                  "password": "N3wP@ssw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    @DisplayName("로그인 상태 비밀번호 변경 API는 현재 비밀번호가 다르면 거절한다")
    void updateMyPasswordRejectsWrongCurrentPassword() throws Exception {
        createMemberThroughApi("password-fail-user", "P@ssw0rd!");
        AuthSession authSession = login("password-fail-user", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        mockMvc.perform(patch("/api/v1/members/me/password")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Wr0ngP@ss!",
                                  "newPassword": "N3wP@ssw0rd!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INVALID_PASSWORD"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "password-fail-user",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    @DisplayName("로그아웃은 refresh token만 폐기하고 access token은 만료 전까지 유지된다")
    void memberAuthLifecycle() throws Exception {
        createMemberThroughApi("tester01", "P@ssw0rd!");
        AuthSession authSession = login("tester01", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());
        AttributeCategory attributeCategory = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10)
        );
        Ingredient ingredient = ingredientRepository.save(
                new Ingredient("PEANUT", "땅콩", true, 10)
        );
        MenuItem menuItem = menuItemRepository.save(
                new MenuItem("PORK_CUTLET", "돈까스", "바삭한 돼지고기 튀김")
        );

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nickname").value(nullValue()))
                .andExpect(jsonPath("$.data.loginId").value("tester01"))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.memberTasteProfile").doesNotExist());

        mockMvc.perform(patch("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "점심탐험가"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.updatedAt").exists());

        mockMvc.perform(patch("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "attributeCategoryIds": [%d],
                                  "restrictionIngredientIds": [%d],
                                  "dislikedMenuItemIds": [%d]
                                }
                                """.formatted(attributeCategory.getId(), ingredient.getId(), menuItem.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").isNumber())
                .andExpect(jsonPath("$.data.profileVersion").value("v1"))
                .andExpect(jsonPath("$.data.attributeCategories[0].code").value("SPICY"))
                .andExpect(jsonPath("$.data.restrictionIngredients[0].code").value("PEANUT"))
                .andExpect(jsonPath("$.data.dislikedMenuItems[0].code").value("PORK_CUTLET"))
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andExpect(jsonPath("$.data.openPersonalRecommendationId").value(nullValue()));

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.memberTasteProfile").doesNotExist());

        assertThat(memberTasteProfileRepository.findByMemberId(
                memberRepository.findByLoginId("tester01").orElseThrow().getId()))
                .isPresent()
                .get()
                .extracting(MemberTasteProfile::getProfileVersion)
                .isEqualTo("v1");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .cookie(authSession.refreshTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loggedOut").value(true))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        assertThat(authRefreshTokenRepository.findByMemberId(
                memberRepository.findByLoginId("tester01").orElseThrow().getId()))
                .isEmpty();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"));

        mockMvc.perform(delete("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INACTIVE_MEMBER"));
    }

    @Test
    @DisplayName("내 취향 프로필 조회는 프로필이 없어도 빈 배열 기반 응답을 반환한다")
    void getMyTasteProfileReturnsEmptyProfile() throws Exception {
        createMemberThroughApi("taste-user-empty", "P@ssw0rd!");
        AuthSession authSession = login("taste-user-empty", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());
        Long memberId = memberRepository.findByLoginId("taste-user-empty").orElseThrow().getId();

        mockMvc.perform(get("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(memberId))
                .andExpect(jsonPath("$.data.profileVersion").value("v1"))
                .andExpect(jsonPath("$.data.attributeCategories.length()").value(0))
                .andExpect(jsonPath("$.data.restrictionIngredients.length()").value(0))
                .andExpect(jsonPath("$.data.dislikedMenuItems.length()").value(0))
                .andExpect(jsonPath("$.data.updatedAt").value(nullValue()));
    }

    @Test
    @DisplayName("내 취향 프로필 조회는 저장된 선택 항목을 표시용 메타데이터와 함께 반환한다")
    void getMyTasteProfileReturnsSelectedItems() throws Exception {
        createMemberThroughApi("taste-user-full", "P@ssw0rd!");
        AuthSession authSession = login("taste-user-full", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());
        Member member = memberRepository.findByLoginId("taste-user-full").orElseThrow();

        AttributeCategory attributeCategory = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10)
        );
        Ingredient ingredient = ingredientRepository.save(
                new Ingredient("PEANUT", "땅콩", true, 10)
        );
        MenuItem menuItem = menuItemRepository.save(
                new MenuItem("PORK_CUTLET", "돈까스", "바삭한 돼지고기 튀김")
        );
        MemberTasteProfile tasteProfile = memberTasteProfileRepository.save(new MemberTasteProfile(member, "v2"));
        memberTasteProfileCategoryRepository.save(new MemberTasteProfileCategory(tasteProfile, attributeCategory));
        memberTasteProfileRestrictionIngredientRepository.save(
                new MemberTasteProfileRestrictionIngredient(tasteProfile, ingredient)
        );
        memberTasteProfileDislikedMenuItemRepository.save(
                new MemberTasteProfileDislikedMenuItem(tasteProfile, menuItem)
        );

        mockMvc.perform(get("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(member.getId()))
                .andExpect(jsonPath("$.data.profileVersion").value("v2"))
                .andExpect(jsonPath("$.data.attributeCategories[0].id").value(attributeCategory.getId()))
                .andExpect(jsonPath("$.data.attributeCategories[0].categoryType").value("FLAVOR"))
                .andExpect(jsonPath("$.data.attributeCategories[0].code").value("SPICY"))
                .andExpect(jsonPath("$.data.restrictionIngredients[0].id").value(ingredient.getId()))
                .andExpect(jsonPath("$.data.restrictionIngredients[0].code").value("PEANUT"))
                .andExpect(jsonPath("$.data.restrictionIngredients[0].allergen").value(true))
                .andExpect(jsonPath("$.data.dislikedMenuItems[0].id").value(menuItem.getId()))
                .andExpect(jsonPath("$.data.dislikedMenuItems[0].code").value("PORK_CUTLET"))
                .andExpect(jsonPath("$.data.dislikedMenuItems[0].name").value("돈까스"))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 잘못된 참조 데이터 ID를 거절한다")
    void updateMyTasteProfileRejectsInvalidReferenceData() throws Exception {
        createMemberThroughApi("taste-user-invalid", "P@ssw0rd!");
        AuthSession authSession = login("taste-user-invalid", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        mockMvc.perform(patch("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "attributeCategoryIds": [999],
                                  "restrictionIngredientIds": [],
                                  "dislikedMenuItemIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INVALID_TASTE_ATTRIBUTE_CATEGORY"));
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 중복된 참조 데이터 ID를 거절한다")
    void updateMyTasteProfileRejectsDuplicateIds() throws Exception {
        createMemberThroughApi("taste-user-duplicate", "P@ssw0rd!");
        AuthSession authSession = login("taste-user-duplicate", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        mockMvc.perform(patch("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "attributeCategoryIds": [1, 1],
                                  "restrictionIngredientIds": [],
                                  "dislikedMenuItemIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_TASTE_ATTRIBUTE_CATEGORY"));
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 잘못된 disliked menu item ID를 거절한다")
    void updateMyTasteProfileRejectsInvalidDislikedMenuItem() throws Exception {
        createMemberThroughApi("taste-user-invalid-menu", "P@ssw0rd!");
        AuthSession authSession = login("taste-user-invalid-menu", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        mockMvc.perform(patch("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "attributeCategoryIds": [],
                                  "restrictionIngredientIds": [],
                                  "dislikedMenuItemIds": [999]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INVALID_TASTE_DISLIKED_MENU_ITEM"));
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 중복 disliked menu item ID를 거절한다")
    void updateMyTasteProfileRejectsDuplicateDislikedMenuItemIds() throws Exception {
        createMemberThroughApi("taste-user-duplicate-menu", "P@ssw0rd!");
        AuthSession authSession = login("taste-user-duplicate-menu", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        mockMvc.perform(patch("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "attributeCategoryIds": [],
                                  "restrictionIngredientIds": [],
                                  "dislikedMenuItemIds": [1001, 1001]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_TASTE_DISLIKED_MENU_ITEM"));
    }

    @Test
    @DisplayName("내 닉네임 수정 시 이미 존재하는 닉네임이면 MEMBER_DUPLICATE_NICKNAME을 반환한다")
    void updateMyProfileFailsWhenNicknameAlreadyExists() throws Exception {
        createMemberThroughApi("tester01", "P@ssw0rd!");
        createMemberThroughApi("tester02", "P@ssw0rd!");

        AuthSession authSession = login("tester01", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        Member duplicateNicknameOwner = memberRepository.findByLoginId("tester02").orElseThrow();
        duplicateNicknameOwner.updateNickname("점심탐험가");
        memberRepository.saveAndFlush(duplicateNicknameOwner);

        mockMvc.perform(patch("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "점심탐험가"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_NICKNAME"));
    }

    @Test
    @DisplayName("보호 API는 토큰 없이 접근하면 AUTH_TOKEN_MISSING을 반환한다")
    void protectedApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("만료된 access token으로 보호 API에 접근하면 AUTH_TOKEN_EXPIRED를 반환한다")
    void protectedApiRejectsExpiredAccessToken() throws Exception {
        createMemberThroughApi("expired-user", "P@ssw0rd!");
        Member member = memberRepository.findByLoginId("expired-user").orElseThrow();
        String expiredAccessToken = expiredAccessToken(member);

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(expiredAccessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_EXPIRED"));
    }

    @Test
    @DisplayName("로그아웃 API는 토큰 없이 접근하면 AUTH_TOKEN_MISSING을 반환한다")
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("로그아웃 시 refresh token 쿠키가 없으면 AUTH_LOGOUT_FAILED를 반환한다")
    void logoutFailsWhenRefreshTokenCookieIsMissing() throws Exception {
        createMemberThroughApi("logout-user", "P@ssw0rd!");
        AuthSession authSession = login("logout-user", "P@ssw0rd!");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("AUTH_LOGOUT_FAILED"));
    }

    @Test
    @DisplayName("refresh token 쿠키로 새 access token과 refresh token을 재발급한다")
    void refreshReissuesTokens() throws Exception {
        createMemberThroughApi("refresh-user", "P@ssw0rd!");
        AuthSession authSession = login("refresh-user", "P@ssw0rd!");
        Long memberId = memberRepository.findByLoginId("refresh-user").orElseThrow().getId();
        String previousRefreshToken = authSession.refreshTokenCookie().getValue();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(authSession.refreshTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.member.id").value(memberId))
                .andExpect(jsonPath("$.data.onboarding.requiredAgreementsCompleted").value(false))
                .andExpect(jsonPath("$.data.onboarding.nicknameCompleted").value(true))
                .andExpect(jsonPath("$.data.onboarding.completed").value(false))
                .andExpect(jsonPath("$.data.onboarding.nextStep").value("REQUIRED_AGREEMENTS"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("matchuri_refresh_token=")))
                .andReturn();

        Cookie rotatedCookie = result.getResponse().getCookie("matchuri_refresh_token");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(previousRefreshToken);

        assertThat(authRefreshTokenRepository.findByToken(previousRefreshToken)).isEmpty();
        assertThat(authRefreshTokenRepository.findByMemberId(memberId))
                .hasSize(1)
                .extracting(AuthRefreshToken::getToken)
                .containsExactly(rotatedCookie.getValue());

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String refreshedAccessToken = body.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(refreshedAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_REQUIRED"));
    }

    @Test
    @DisplayName("최신 필수 약관 동의 기록이 있으면 구 access token claim도 fallback 검증으로 허용한다")
    void protectedApiAllowsLegacyTokenWhenRequiredAgreementsAlreadyCompleted() throws Exception {
        Member member = memberRepository.save(new Member(
                "legacy-agreement-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.TERMS_OF_SERVICE, "2026-04-10"));
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.PRIVACY_POLICY, "2026-04-10"));

        String legacyAccessToken = accessToken(member, null, Instant.now().plusSeconds(1800));

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(legacyAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(member.getId()));
    }

    @Test
    @DisplayName("refresh token 쿠키가 없으면 AUTH_REFRESH_TOKEN_MISSING을 반환한다")
    void refreshFailsWhenRefreshTokenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REFRESH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("유효하지 않은 refresh token이면 쿠키를 비우고 AUTH_REFRESH_TOKEN_INVALID를 반환한다")
    void refreshFailsWhenRefreshTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("matchuri_refresh_token", "missing-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REFRESH_TOKEN_INVALID"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("만료된 refresh token이면 쿠키를 비우고 AUTH_REFRESH_TOKEN_EXPIRED를 반환한다")
    void refreshFailsWhenRefreshTokenIsExpired() throws Exception {
        Member member = memberRepository.save(new Member(
                "expired-refresh-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        authRefreshTokenRepository.save(AuthRefreshToken.issue(
                member,
                "expired-refresh-token",
                LocalDateTime.now().minusMinutes(1)
        ));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("matchuri_refresh_token", "expired-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REFRESH_TOKEN_EXPIRED"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("구글 OAuth2 시작 요청은 Spring Security authorization 엔드포인트로 리다이렉트한다")
    void startGoogleOAuth2LoginRedirects() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/oauth2/authorization/google"));
    }

    @Test
    @DisplayName("카카오 OAuth2 시작 요청은 Spring Security authorization 엔드포인트로 리다이렉트한다")
    void startKakaoOAuth2LoginRedirects() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/kakao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/oauth2/authorization/kakao"));
    }

    @Test
    @DisplayName("네이버 OAuth2 시작 요청은 Spring Security authorization 엔드포인트로 리다이렉트한다")
    void startNaverOAuth2LoginRedirects() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/naver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/oauth2/authorization/naver"));
    }

    @Test
    @DisplayName("지원하지 않는 OAuth2 provider 시작 요청은 거절한다")
    void startOAuth2LoginRejectsUnsupportedProvider() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/apple"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("AUTH_OAUTH2_PROVIDER_NOT_SUPPORTED"));
    }

    @Test
    @DisplayName("Spring Security OAuth2 authorization 엔드포인트는 서버 세션을 사용하고 대형 커스텀 쿠키를 만들지 않는다")
    void authorizationEndpointUsesServerSideSessionStorage() throws Exception {
        MvcResult result = mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        org.hamcrest.Matchers.containsString("https://accounts.google.com/o/oauth2/v2/auth")))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
        assertThat(result.getResponse().getCookie("matchuri_oauth2_auth_request")).isNull();
        if (result.getResponse().getHeader(HttpHeaders.SET_COOKIE) != null) {
            assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                    .doesNotContain("matchuri_oauth2_auth_request=");
        }
    }

    @Test
    @DisplayName("카카오 Spring Security OAuth2 authorization 엔드포인트는 카카오 인증 서버로 리다이렉트한다")
    void kakaoAuthorizationEndpointRedirectsToKakao() throws Exception {
        MvcResult result = mockMvc.perform(get("/oauth2/authorization/kakao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        org.hamcrest.Matchers.containsString("https://kauth.kakao.com/oauth/authorize")))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
        assertThat(result.getResponse().getCookie("matchuri_oauth2_auth_request")).isNull();
    }

    @Test
    @DisplayName("네이버 Spring Security OAuth2 authorization 엔드포인트는 네이버 인증 서버로 리다이렉트한다")
    void naverAuthorizationEndpointRedirectsToNaver() throws Exception {
        MvcResult result = mockMvc.perform(get("/oauth2/authorization/naver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        org.hamcrest.Matchers.containsString("https://nid.naver.com/oauth2.0/authorize")))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
        assertThat(result.getResponse().getCookie("matchuri_oauth2_auth_request")).isNull();
    }

    @Test
    @DisplayName("유효한 소셜 교환 코드는 액세스 토큰으로 교환된다")
    void exchangeOAuth2Code() throws Exception {
        Member member = memberRepository.save(
                Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-1", "google@example.com",
                        "example_google"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "valid-exchange-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "valid-exchange-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.member.id").value(member.getId()))
                .andExpect(jsonPath("$.data.member.nickname").value("example_google"))
                .andExpect(jsonPath("$.data.onboarding.requiredAgreementsCompleted").value(false))
                .andExpect(jsonPath("$.data.onboarding.nicknameCompleted").value(false))
                .andExpect(jsonPath("$.data.onboarding.completed").value(false))
                .andExpect(jsonPath("$.data.onboarding.nextStep").value("REQUIRED_AGREEMENTS"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = body.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_REQUIRED"));
    }

    @Test
    @DisplayName("유효한 카카오 소셜 교환 코드는 액세스 토큰으로 교환된다")
    void exchangeKakaoOAuth2Code() throws Exception {
        Member member = memberRepository.save(
                Member.createSocialMember(SocialProviderType.KAKAO, "123456789", "kakao@example.com",
                        "kakao_kakao"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.KAKAO,
                "valid-kakao-exchange-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "KAKAO",
                                  "code": "valid-kakao-exchange-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.member.id").value(member.getId()))
                .andExpect(jsonPath("$.data.member.nickname").value("kakao_kakao"))
                .andExpect(jsonPath("$.data.onboarding.nextStep").value("REQUIRED_AGREEMENTS"));
    }

    @Test
    @DisplayName("유효한 네이버 소셜 교환 코드는 액세스 토큰으로 교환된다")
    void exchangeNaverOAuth2Code() throws Exception {
        Member member = memberRepository.save(
                Member.createSocialMember(SocialProviderType.NAVER, "naver-user-1", "naver@example.com",
                        "naver_naver"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.NAVER,
                "valid-naver-exchange-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "NAVER",
                                  "code": "valid-naver-exchange-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.member.id").value(member.getId()))
                .andExpect(jsonPath("$.data.member.nickname").value("naver_naver"))
                .andExpect(jsonPath("$.data.onboarding.nextStep").value("REQUIRED_AGREEMENTS"));
    }

    @Test
    @DisplayName("약관 완료 후 닉네임 미완료 회원은 핵심 API에서 차단되지만 닉네임 수정은 가능하다")
    void nicknameIncompleteMemberCanPatchProfileBeforeProtectedApiAccess() throws Exception {
        Member member = memberRepository.save(Member.createSocialMember(
                SocialProviderType.GOOGLE,
                "google-user-nickname-required",
                "nickname-required@example.com",
                "nickname_required_google"
        ));
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.TERMS_OF_SERVICE, "2026-04-10"));
        memberAgreementRepository.save(MemberAgreement.create(member, AgreementType.PRIVACY_POLICY, "2026-04-10"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "nickname-required-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "nickname-required-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboarding.requiredAgreementsCompleted").value(true))
                .andExpect(jsonPath("$.data.onboarding.nicknameCompleted").value(false))
                .andExpect(jsonPath("$.data.onboarding.completed").value(false))
                .andExpect(jsonPath("$.data.onboarding.nextStep").value("REQUIRED_NICKNAME"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = body.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_NICKNAME_REQUIRED"));

        mockMvc.perform(patch("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "점심결정러"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.onboarding.requiredAgreementsCompleted").value(true))
                .andExpect(jsonPath("$.data.onboarding.nicknameCompleted").value(true))
                .andExpect(jsonPath("$.data.onboarding.completed").value(true))
                .andExpect(jsonPath("$.data.onboarding.nextStep").value("READY"));

        assertThat(memberRepository.findById(member.getId()).orElseThrow().isNicknameCompleted()).isTrue();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value(nullValue()))
                .andExpect(jsonPath("$.data.nickname").value("점심결정러"))
                .andExpect(jsonPath("$.data.isSocial").value(true));
    }

    @Test
    @DisplayName("소셜 교환 코드는 한 번 사용 후 재사용할 수 없다")
    void exchangeOAuth2CodeCannotBeReused() throws Exception {
        Member member = memberRepository.save(
                Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-2", "google2@example.com",
                        "google2_google"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "single-use-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "single-use-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "single-use-code"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_OAUTH2_EXCHANGE_CODE_INVALID"));
    }

    @Test
    @DisplayName("만료된 소셜 교환 코드는 AUTH_OAUTH2_EXCHANGE_CODE_INVALID를 반환한다")
    void exchangeOAuth2CodeFailsWhenCodeIsExpired() throws Exception {
        Member member = memberRepository.save(
                Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-3", "google3@example.com",
                        "google3_google"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "expired-exchange-code",
                LocalDateTime.now().minusMinutes(1)
        ));

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "expired-exchange-code"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_OAUTH2_EXCHANGE_CODE_INVALID"));
    }

    @Test
    @DisplayName("유효하지 않은 소셜 교환 코드는 AUTH_OAUTH2_EXCHANGE_CODE_INVALID를 반환한다")
    void exchangeOAuth2CodeFailsWhenCodeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "missing-code"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_OAUTH2_EXCHANGE_CODE_INVALID"));
    }

    @Test
    @DisplayName("탈퇴한 회원은 다시 로컬 로그인할 수 없다")
    void withdrawnMemberCannotLoginAgain() throws Exception {
        createMemberThroughApi("withdrawn-user", "P@ssw0rd!");
        AuthSession authSession = login("withdrawn-user", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        mockMvc.perform(delete("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "withdrawn-user",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INACTIVE_MEMBER"));
    }

    @Test
    @DisplayName("허용된 Origin의 preflight 요청은 CORS 헤더를 반환한다")
    void preflightReturnsCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
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

    private String issueSignupEmailVerificationToken(String email) {
        String token = "ev_test_" + email.replace("@", "_").replace(".", "_");
        EmailVerification verification = EmailVerification.issue(
                email,
                null,
                EmailVerificationPurpose.SIGNUP,
                "hashed-code",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now()
        );
        verification.verify(
                emailVerificationTokenGenerator.hashToken(token),
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now()
        );
        emailVerificationRepository.save(verification);
        return token;
    }

    private AuthSession login(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "%s",
                                  "password": "%s"
                                }
                                """.formatted(loginId, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.onboarding.nextStep").exists())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("matchuri_refresh_token=")))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new AuthSession(
                body.path("data").path("accessToken").asText(),
                result.getResponse().getCookie("matchuri_refresh_token")
        );
    }

    private String submitRequiredAgreements(String accessToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
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
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("accessToken").asText();
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String expiredAccessToken(Member member) {
        Instant now = Instant.now();
        Instant issuedAt = now.minusSeconds(3600);
        Instant expiredAt = now.minusSeconds(1800);
        return accessToken(member, null, expiredAt, issuedAt);
    }

    private String accessToken(Member member, String requiredAgreementRevision, Instant expiresAt) {
        return accessToken(member, requiredAgreementRevision, expiresAt, Instant.now());
    }

    private String accessToken(Member member, String requiredAgreementRevision, Instant expiresAt, Instant issuedAt) {
        SecretKey signingKey = Keys.hmacShaKeyFor(
                matchuriProperties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .issuer(matchuriProperties.getAuth().getJwt().getIssuer())
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getMemberRole().name())
                .claim("loginId", member.getLoginId())
                .claim("requiredAgreementRevision", requiredAgreementRevision)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    private record AuthSession(
            String accessToken,
            Cookie refreshTokenCookie
    ) {
    }
}
