package matchuri.backend.api.menu;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminPageSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @BeforeEach
    void setUp() {
        menuItemRepository.deleteAll();
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("미인증 사용자는 admin 화면 접근 시 로그인 화면으로 이동한다")
    void adminPageRedirectsAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    @DisplayName("DB에 저장된 활성 ADMIN 계정은 admin 화면에 로그인할 수 있다")
    void adminCanLoginWithDbAccount() throws Exception {
        createAdmin();

        loginAsAdmin()
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("ADMIN 세션은 주요 admin 화면을 렌더링할 수 있다")
    void adminSessionCanRenderAdminPages() throws Exception {
        createAdmin();
        MenuItem menuItem = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "밥과 나물 메뉴"));
        attributeCategoryRepository.save(new AttributeCategory(CategoryType.FOOD_CATEGORY, "KOREAN", "한식", 10));
        ingredientRepository.save(new Ingredient("EGG", "달걀", true, 10));

        MockHttpSession session = (MockHttpSession) loginAsAdmin()
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/admin/menu-items").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/menu-items/new").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/menu-items/{menuItemId}", menuItem.getId()).session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/attribute-categories").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/ingredients").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/preset-profile-images").session(session))
                .andExpect(status().isOk());
    }

    private void createAdmin() {
        memberRepository.save(new Member(
                "admin01",
                passwordEncoder.encode("P@ssw0rd!"),
                "admin@example.com",
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
    }

    private org.springframework.test.web.servlet.ResultActions loginAsAdmin() throws Exception {
        var loginPage = mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrfToken = (CsrfToken) loginPage.getRequest().getAttribute(CsrfToken.class.getName());
        MockHttpSession session = (MockHttpSession) loginPage.getRequest().getSession(false);

        return mockMvc.perform(post("/admin/login")
                .session(session)
                .param("username", "admin01")
                .param("password", "P@ssw0rd!")
                .param(csrfToken.getParameterName(), csrfToken.getToken()));
    }
}
