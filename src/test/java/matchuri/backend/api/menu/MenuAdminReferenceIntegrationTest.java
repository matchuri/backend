package matchuri.backend.api.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuAdminReferenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @BeforeEach
    void setUp() {
        attributeCategoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("관리자 attribute category 목록 조회는 활성과 비활성 데이터를 함께 정렬해서 반환한다")
    void getAdminAttributeCategoriesReturnsAllRowsInSortedOrder() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        AttributeCategory spicy = attributeCategoryRepository.save(new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 20));
        AttributeCategory grilled = attributeCategoryRepository.save(new AttributeCategory(CategoryType.COOKING_METHOD, "GRILLED", "구이", 10));
        AttributeCategory mild = new AttributeCategory(CategoryType.FLAVOR, "MILD", "순한맛", 10);
        mild.deactivate();
        mild = attributeCategoryRepository.save(mild);

        mockMvc.perform(get("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(grilled.getId()))
                .andExpect(jsonPath("$.data[0].categoryType").value("COOKING_METHOD"))
                .andExpect(jsonPath("$.data[0].isActive").value(true))
                .andExpect(jsonPath("$.data[1].id").value(mild.getId()))
                .andExpect(jsonPath("$.data[1].code").value("MILD"))
                .andExpect(jsonPath("$.data[1].isActive").value(false))
                .andExpect(jsonPath("$.data[2].id").value(spicy.getId()))
                .andExpect(jsonPath("$.data[2].code").value("SPICY"))
                .andExpect(jsonPath("$.data[2].isActive").value(true));
    }

    @Test
    @DisplayName("일반 회원은 관리자 attribute category 목록 조회에 접근할 수 없다")
    void getAdminAttributeCategoriesRejectsNonAdminMember() throws Exception {
        Member member = memberRepository.save(new Member(
                "member-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(get("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("관리자는 attribute category를 생성할 수 있다")
    void createAdminAttributeCategory() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-create-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryType": "flavor",
                                  "code": "SPICY",
                                  "name": " 매운맛 ",
                                  "sortOrder": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.categoryType").value("FLAVOR"))
                .andExpect(jsonPath("$.data.code").value("SPICY"))
                .andExpect(jsonPath("$.data.name").value("매운맛"))
                .andExpect(jsonPath("$.data.sortOrder").value(10))
                .andExpect(jsonPath("$.data.isActive").value(true));

        assertThat(attributeCategoryRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(attributeCategory -> {
                    assertThat(attributeCategory.getCategoryType()).isEqualTo(CategoryType.FLAVOR);
                    assertThat(attributeCategory.getCode()).isEqualTo("SPICY");
                    assertThat(attributeCategory.getName()).isEqualTo("매운맛");
                    assertThat(attributeCategory.isActive()).isTrue();
                });
    }

    @Test
    @DisplayName("관리자 attribute category 생성은 같은 categoryType과 code 조합 중복을 거절한다")
    void createAdminAttributeCategoryRejectsDuplicate() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-duplicate-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        attributeCategoryRepository.save(new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));

        mockMvc.perform(post("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryType": "FLAVOR",
                                  "code": "SPICY",
                                  "name": "새 매운맛",
                                  "sortOrder": 20
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_ATTRIBUTE_CATEGORY_DUPLICATE"));
    }

    @Test
    @DisplayName("관리자 attribute category 생성은 잘못된 categoryType을 공통 바디 검증 오류로 거절한다")
    void createAdminAttributeCategoryRejectsInvalidCategoryType() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-invalid-type-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryType": "INVALID_TYPE",
                                  "code": "SPICY",
                                  "name": "매운맛",
                                  "sortOrder": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_BODY_FIELD"))
                .andExpect(jsonPath("$.error.details[0].field").value("categoryType"));
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String accessToken(Member member) {
        SecretKey signingKey = Keys.hmacShaKeyFor(
                matchuriProperties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .issuer(matchuriProperties.getAuth().getJwt().getIssuer())
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getMemberRole().name())
                .claim("loginId", member.getLoginId())
                .claim("requiredAgreementRevision", RequiredAgreementVersions.currentRevision())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(1800)))
                .signWith(signingKey)
                .compact();
    }
}
