package matchuri.backend.api.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.MenuItemImageRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.global.config.MatchuriProperties;
import matchuri.backend.infra.storage.ObjectStorageClient;
import matchuri.backend.infra.storage.UploadObjectCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuImageAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuItemImageRepository menuItemImageRepository;

    @Autowired
    private ImageAssetRepository imageAssetRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @MockitoBean
    private ObjectStorageClient objectStorageClient;

    @BeforeEach
    void setUp() {
        cleanUp();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        menuItemImageRepository.deleteAll();
        imageAssetRepository.deleteAll();
        menuItemRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("관리자는 메뉴 대표 이미지를 업로드할 수 있다")
    void uploadPrimaryMenuImage() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-image-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        MenuItem menuItem = menuItemRepository.save(new MenuItem("PORK_CUTLET", "돈까스", "바삭한 메뉴"));

        mockMvc.perform(multipart("/api/v1/admin/menu-items/{menuItemId}/images", menuItem.getId())
                        .file(pngFile("menu.png", 320, 320))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.menuId").value(menuItem.getId()))
                .andExpect(jsonPath("$.data.thumbnailUrl").value(org.hamcrest.Matchers.startsWith(
                        "https://asset.matchuri.com/menu-items/" + menuItem.getId() + "/")))
                .andExpect(jsonPath("$.data.contentType").value("image/png"))
                .andExpect(jsonPath("$.data.width").value(320))
                .andExpect(jsonPath("$.data.height").value(320));

        assertThat(menuItemImageRepository.findByMenuId(menuItem.getId())).isPresent();
        assertThat(imageAssetRepository.findAll()).hasSize(1);
        verify(objectStorageClient).upload(any(UploadObjectCommand.class));
    }

    @Test
    @DisplayName("관리자 메뉴 이미지 업로드는 GIF를 거절한다")
    void uploadPrimaryMenuImageRejectsGif() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-gif-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        MenuItem menuItem = menuItemRepository.save(new MenuItem("SUSHI", "초밥", "생선 메뉴"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "menu.gif",
                "image/gif",
                "GIF89a".getBytes(StandardCharsets.US_ASCII)
        );

        mockMvc.perform(multipart("/api/v1/admin/menu-items/{menuItemId}/images", menuItem.getId())
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("IMAGE_UNSUPPORTED_CONTENT_TYPE"));
    }

    @Test
    @DisplayName("관리자는 메뉴 대표 이미지 연결을 제거할 수 있다")
    void deletePrimaryMenuImage() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-delete-image-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        MenuItem menuItem = menuItemRepository.save(new MenuItem("RICE_NOODLE", "쌀국수", "면 메뉴"));
        mockMvc.perform(multipart("/api/v1/admin/menu-items/{menuItemId}/images", menuItem.getId())
                        .file(pngFile("menu.png", 320, 320))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/menu-items/{menuItemId}/images/primary", menuItem.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(menuItemImageRepository.findByMenuId(menuItem.getId())).isEmpty();
    }

    private MockMultipartFile pngFile(String filename, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);

        return new MockMultipartFile(
                "file",
                filename,
                "image/png",
                outputStream.toByteArray()
        );
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
