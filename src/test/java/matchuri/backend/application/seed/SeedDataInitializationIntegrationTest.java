package matchuri.backend.application.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import matchuri.backend.recommendation.entity.PersonalRecommendation;
import matchuri.backend.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.recommendation.repository.PersonalRecommendationRepository;
import matchuri.backend.recommendation.repository.PersonalRecommendationCandidateRepository;
import matchuri.backend.groupdecision.repository.GroupLocationRepository;
import matchuri.backend.media.entity.ImageAsset;
import matchuri.backend.media.entity.ImageStorageProvider;
import matchuri.backend.media.repository.ImageAssetRepository;
import matchuri.backend.media.repository.PresetProfileImageRepository;
import matchuri.backend.groupdecision.repository.GroupRoomMemberRepository;
import matchuri.backend.groupdecision.repository.GroupRoomRepository;
import matchuri.backend.identity.member.repository.MemberAgreementRepository;
import matchuri.backend.identity.member.repository.MemberProfileImageRepository;
import matchuri.backend.identity.member.repository.MemberRepository;
import matchuri.backend.identity.member.repository.MemberTasteProfileCategoryRepository;
import matchuri.backend.identity.member.repository.MemberTasteProfileDislikedMenuItemRepository;
import matchuri.backend.identity.member.repository.MemberTasteProfileRepository;
import matchuri.backend.identity.member.repository.MemberTasteProfileRestrictionIngredientRepository;
import matchuri.backend.identity.member.entity.Member;
import matchuri.backend.identity.member.entity.MemberRole;
import matchuri.backend.identity.member.entity.MemberStatus;
import matchuri.backend.catalog.entity.MenuItem;
import matchuri.backend.catalog.entity.MenuItemImage;
import matchuri.backend.catalog.repository.AttributeCategoryRepository;
import matchuri.backend.catalog.repository.IngredientRepository;
import matchuri.backend.catalog.repository.MenuAttributeCategoryRepository;
import matchuri.backend.catalog.repository.MenuIngredientRepository;
import matchuri.backend.catalog.repository.MenuItemImageRepository;
import matchuri.backend.catalog.repository.MenuItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:seed-initialization;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("test")
@Transactional
class SeedDataInitializationIntegrationTest {

    @Autowired
    private SeedDataResourceLoader seedDataResourceLoader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PersonalRecommendationRepository personalRecommendationRepository;

    @Autowired
    private PersonalRecommendationCandidateRepository personalRecommendationCandidateRepository;

    @Autowired
    private ReferenceDataSeedService referenceDataSeedService;

    @Autowired
    private LocalSampleDataSeedService localSampleDataSeedService;

    @Autowired
    private LocalMenuImageSeedService localMenuImageSeedService;

    @Autowired
    private PresetProfileImageSeedService presetProfileImageSeedService;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuAttributeCategoryRepository menuAttributeCategoryRepository;

    @Autowired
    private MenuIngredientRepository menuIngredientRepository;

    @Autowired
    private MenuItemImageRepository menuItemImageRepository;

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
    private MemberTasteProfileCategoryRepository memberTasteProfileCategoryRepository;

    @Autowired
    private MemberTasteProfileRestrictionIngredientRepository restrictionIngredientRepository;

    @Autowired
    private MemberTasteProfileDislikedMenuItemRepository dislikedMenuItemRepository;

    @Autowired
    private GroupRoomRepository groupRoomRepository;

    @Autowired
    private GroupRoomMemberRepository groupRoomMemberRepository;

    @Autowired
    private GroupLocationRepository groupLocationRepository;

    @Test
    @DisplayName("기준 데이터와 로컬 샘플 데이터 및 메뉴 이미지를 멱등하게 생성한다")
    void initializesSeedDataIdempotently() {
        MenuItem existingMenu = menuItemRepository.save(
                new MenuItem("BIBIMBAP", "기존 비빔밥", "기존 메뉴 설명")
        );
        ImageAsset existingImage = imageAssetRepository.save(new ImageAsset(
                ImageStorageProvider.CLOUDFLARE_R2,
                "test-bucket",
                "menu-items/custom/bibimbap.png",
                "custom-bibimbap.png",
                "image/png",
                1024,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                300,
                300
        ));
        menuItemImageRepository.save(new MenuItemImage(existingMenu, existingImage));
        Member existingMember = memberRepository.save(Member.builder()
                .loginId("tester01")
                .passwordHash("existing-password-hash")
                .nickname("기존 회원")
                .nicknameCompleted(true)
                .email("existing-tester01@matchuri.test")
                .social(false)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build());
        PersonalRecommendation existingRecommendation = personalRecommendationRepository.save(
                PersonalRecommendation.of(existingMember));
        PersonalRecommendationCandidate existingCandidate = personalRecommendationCandidateRepository.save(
                PersonalRecommendationCandidate.of(existingRecommendation, existingMenu, 1, 87.5));
        existingRecommendation.select(existingCandidate, LocalDateTime.now());
        personalRecommendationRepository.saveAndFlush(existingRecommendation);

        referenceDataSeedService.initialize();
        presetProfileImageSeedService.initialize();
        localMenuImageSeedService.initialize();
        localSampleDataSeedService.initialize();
        Map<String, List<Map<String, Object>>> existingLinks = catalogAndMemberLinks();
        referenceDataSeedService.initialize();
        presetProfileImageSeedService.initialize();
        localMenuImageSeedService.initialize();
        localSampleDataSeedService.initialize();

        ReferenceSeedData seed = seedDataResourceLoader.load("seed/reference-data.json", ReferenceSeedData.class);
        assertThat(attributeCategoryRepository.count()).isEqualTo(seed.attributeCategories().size());
        assertThat(ingredientRepository.count()).isEqualTo(seed.ingredients().size());
        assertThat(menuItemRepository.count()).isEqualTo(seed.menuItems().size());
        assertThat(menuAttributeCategoryRepository.count()).isEqualTo(seed.menuAttributeCategories().size());
        assertThat(menuIngredientRepository.count()).isEqualTo(seed.menuIngredients().size());
        assertThat(catalogAndMemberLinks()).isEqualTo(existingLinks);
        assertThat(menuItemImageRepository.count()).isEqualTo(33);
        assertThat(imageAssetRepository.count()).isEqualTo(40);
        assertThat(presetProfileImageRepository.count()).isEqualTo(7);
        assertThat(presetProfileImageRepository.findActiveDefaults()).singleElement()
                .satisfies(preset -> assertThat(preset.getImageAsset().getObjectKey())
                        .isEqualTo("preset-profile/v1-spaghetti.png"));

        assertThat(memberRepository.count()).isEqualTo(5);
        assertThat(memberProfileImageRepository.count()).isEqualTo(5);
        assertThat(memberAgreementRepository.count()).isEqualTo(10);
        assertThat(memberTasteProfileRepository.count()).isEqualTo(4);
        assertThat(memberTasteProfileCategoryRepository.count()).isEqualTo(16);
        assertThat(restrictionIngredientRepository.count()).isEqualTo(4);
        assertThat(dislikedMenuItemRepository.count()).isEqualTo(7);
        assertThat(groupRoomRepository.count()).isEqualTo(2);
        assertThat(groupRoomMemberRepository.count()).isEqualTo(5);
        assertThat(groupLocationRepository.count()).isEqualTo(2);
        assertThat(menuItemRepository.findByCode("BIBIMBAP")).get()
                .extracting(MenuItem::getName, MenuItem::getDescription)
                .containsExactly("기존 비빔밥", "기존 메뉴 설명");
        assertThat(menuItemImageRepository.findByMenuId(existingMenu.getId())).get()
                .extracting(menuItemImage -> menuItemImage.getImageAsset().getObjectKey())
                .isEqualTo("menu-items/custom/bibimbap.png");
        assertThat(menuItemRepository.findByCode("KIMCHI_STEW"))
                .flatMap(menuItem -> menuItemImageRepository.findByMenuId(menuItem.getId()))
                .get()
                .extracting(menuItemImage -> menuItemImage.getImageAsset().getObjectKey())
                .isEqualTo("menu-items/1/530de6c1-86ac-47cf-85a0-8d402363c259.png");
        assertThat(memberRepository.findByLoginId("tester01")).get()
                .extracting(Member::getNickname)
                .isEqualTo("기존 회원");
        assertThat(personalRecommendationRepository.findById(existingRecommendation.getId()).orElseThrow()
                .getSelectedCandidate().getId()).isEqualTo(existingCandidate.getId());
        assertThat(personalRecommendationCandidateRepository.findById(existingCandidate.getId()).orElseThrow())
                .extracting(candidate -> candidate.getMenuItem().getId(), PersonalRecommendationCandidate::getScore)
                .containsExactly(existingMenu.getId(), 87.5);
    }

    private Map<String, List<Map<String, Object>>> catalogAndMemberLinks() {
        menuItemRepository.flush();
        Map<String, List<Map<String, Object>>> snapshot = new LinkedHashMap<>();
        for (String table : List.of("attribute_categories", "ingredients", "menu_items",
                "menu_attribute_categories", "menu_ingredients", "menu_item_images", "image_assets",
                "member_taste_profiles", "member_taste_profile_categories",
                "member_taste_profile_restriction_ingredients", "member_taste_profile_disliked_menu_items",
                "personal_recommendations", "personal_recommendation_candidates")) {
            snapshot.put(table, jdbcTemplate.queryForList("select * from " + table + " order by id"));
        }
        return snapshot;
    }
}
