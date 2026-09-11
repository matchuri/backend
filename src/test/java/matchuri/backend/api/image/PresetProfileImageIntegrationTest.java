package matchuri.backend.api.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.image.entity.PresetProfileImage;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.image.repository.PresetProfileImageRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberProfileImage;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberProfileImageRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.global.config.MatchuriProperties;
import matchuri.backend.infra.storage.ObjectStorageClient;
import matchuri.backend.infra.storage.UploadObjectCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class PresetProfileImageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileImageRepository memberProfileImageRepository;

    @Autowired
    private PresetProfileImageRepository presetProfileImageRepository;

    @Autowired
    private ImageAssetRepository imageAssetRepository;

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

    @Test
    @DisplayName("회원은 프리셋 설정 시 기존 연결 한 건을 교체하고 프로필 URL을 조회한다")
    void memberReplacesPresetWithoutHistory() throws Exception {
        PresetProfileImage defaultPreset = createPreset("preset-profile/default.png", true);
        PresetProfileImage selectedPreset = createPreset("preset-profile/selected.png", false);
        Member member = createMember("profile-member", MemberRole.MEMBER);
        memberProfileImageRepository.save(new MemberProfileImage(member, defaultPreset.getImageAsset()));

        mockMvc.perform(put("/api/v1/members/profile/preset-image")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "presetProfileImageId": %d
                                }
                                """.formatted(selectedPreset.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.presetProfileImageId").value(selectedPreset.getId()))
                .andExpect(jsonPath("$.data.imageUrl")
                        .value("https://asset.matchuri.com/preset-profile/selected.png"));

        assertThat(memberProfileImageRepository.findAll()).hasSize(1);
        assertThat(memberProfileImageRepository.findByMemberId(member.getId())).get()
                .extracting(profileImage -> profileImage.getImageAsset().getId())
                .isEqualTo(selectedPreset.getImageAsset().getId());

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileImageUrl")
                        .value("https://asset.matchuri.com/preset-profile/selected.png"));
    }

    @Test
    @DisplayName("회원은 삭제되지 않은 프리셋 목록을 ID 순서로 조회한다")
    void memberGetsActivePresetImagesInIdOrder() throws Exception {
        PresetProfileImage defaultPreset = createPreset("preset-profile/default.png", true);
        PresetProfileImage selectablePreset = createPreset("preset-profile/selectable.png", false);
        PresetProfileImage deletedPreset = createPreset("preset-profile/deleted.png", false);
        deletedPreset.delete();
        presetProfileImageRepository.save(deletedPreset);
        Member member = createMember("preset-list-member", MemberRole.MEMBER);

        mockMvc.perform(get("/api/v1/members/profile/preset-image")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].presetProfileImageId").value(defaultPreset.getId()))
                .andExpect(jsonPath("$.data[0].imageUrl")
                        .value("https://asset.matchuri.com/preset-profile/default.png"))
                .andExpect(jsonPath("$.data[0].isDefault").value(true))
                .andExpect(jsonPath("$.data[0].objectKey").doesNotExist())
                .andExpect(jsonPath("$.data[1].presetProfileImageId").value(selectablePreset.getId()))
                .andExpect(jsonPath("$.data[1].isDefault").value(false));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 프리셋 목록을 조회할 수 없다")
    void unauthenticatedMemberCannotGetPresetImages() throws Exception {
        mockMvc.perform(get("/api/v1/members/profile/preset-image"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("탈퇴 대기 회원은 프리셋 목록을 조회할 수 없다")
    void deletedMemberCannotGetPresetImages() throws Exception {
        Member member = createMember("inactive-preset-list-member", MemberRole.MEMBER);
        member.withdraw(LocalDateTime.now(), LocalDateTime.now().plusDays(3));
        memberRepository.save(member);

        mockMvc.perform(get("/api/v1/members/profile/preset-image")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INACTIVE_MEMBER"));
    }

    @Test
    @DisplayName("관리자는 기존 기본을 해제하고 선택한 프리셋 하나만 기본으로 설정한다")
    void adminSetsExactlyOneDefault() throws Exception {
        PresetProfileImage first = createPreset("preset-profile/first.png", true);
        PresetProfileImage second = createPreset("preset-profile/second.png", false);
        Member admin = createMember("profile-admin", MemberRole.ADMIN);

        mockMvc.perform(put("/api/v1/admin/preset-profile-images/{presetProfileImageId}/default", second.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(second.getId()))
                .andExpect(jsonPath("$.data.isDefault").value(true));

        assertThat(presetProfileImageRepository.findActiveDefaults())
                .extracting(PresetProfileImage::getId)
                .containsExactly(second.getId());
        assertThat(presetProfileImageRepository.findById(first.getId())).get()
                .extracting(PresetProfileImage::isDefault)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("관리자는 비기본 프리셋을 soft delete할 수 있다")
    void adminDeletesNonDefaultPreset() throws Exception {
        createPreset("preset-profile/default.png", true);
        PresetProfileImage target = createPreset("preset-profile/delete.png", false);
        Member admin = createMember("delete-profile-admin", MemberRole.ADMIN);

        mockMvc.perform(delete("/api/v1/admin/preset-profile-images/{presetProfileImageId}", target.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(presetProfileImageRepository.findById(target.getId())).get()
                .extracting(PresetProfileImage::isDeleted)
                .isEqualTo(true);
        assertThat(target.getImageAsset().getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("관리자는 현재 기본 프리셋을 삭제할 수 없다")
    void adminCannotDeleteDefaultPreset() throws Exception {
        PresetProfileImage target = createPreset("preset-profile/default.png", true);
        Member admin = createMember("default-delete-admin", MemberRole.ADMIN);

        mockMvc.perform(delete("/api/v1/admin/preset-profile-images/{presetProfileImageId}", target.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value("IMAGE_DEFAULT_PRESET_PROFILE_DELETE_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("관리자는 이미지 파일을 새 비기본 프리셋으로 업로드할 수 있다")
    void adminUploadsPreset() throws Exception {
        Member admin = createMember("upload-profile-admin", MemberRole.ADMIN);

        mockMvc.perform(multipart("/api/v1/admin/preset-profile-images")
                        .file(pngFile("profile.png", 320, 320))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault").value(false))
                .andExpect(jsonPath("$.data.imageUrl")
                        .value(org.hamcrest.Matchers.startsWith(
                                "https://asset.matchuri.com/preset-profile/")));

        assertThat(presetProfileImageRepository.findAllActive()).hasSize(1);
        verify(objectStorageClient).upload(any(UploadObjectCommand.class));
    }

    @Test
    @DisplayName("일반 회원은 관리자 프리셋 API에 접근할 수 없다")
    void memberCannotAccessAdminPresetApi() throws Exception {
        Member member = createMember("non-admin-profile", MemberRole.MEMBER);

        mockMvc.perform(get("/api/v1/admin/preset-profile-images")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isForbidden());
    }

    private PresetProfileImage createPreset(String objectKey, boolean isDefault) {
        ImageAsset asset = imageAssetRepository.save(new ImageAsset(
                ImageStorageProvider.CLOUDFLARE_R2,
                "test-bucket",
                objectKey,
                objectKey.substring(objectKey.lastIndexOf('/') + 1),
                "image/png",
                1024,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                320,
                320
        ));
        return presetProfileImageRepository.save(new PresetProfileImage(asset, isDefault));
    }

    private Member createMember(String loginId, MemberRole role) {
        return memberRepository.save(new Member(
                loginId,
                "hashed-password",
                null,
                false,
                null,
                null,
                role,
                MemberStatus.ACTIVE
        ));
    }

    private MockMultipartFile pngFile(String filename, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return new MockMultipartFile("file", filename, "image/png", outputStream.toByteArray());
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

    private void cleanUp() {
        memberProfileImageRepository.deleteAll();
        memberRepository.deleteAll();
        presetProfileImageRepository.deleteAll();
        imageAssetRepository.deleteAll();
    }
}
