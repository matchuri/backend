package matchuri.backend.infra.seed;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.domain.group.repository.GroupLocationRepository;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.image.repository.PresetProfileImageRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberProfileImageRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileCategoryRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileDislikedMenuItemRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRestrictionIngredientRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemImageRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeedDataInitializationIntegrationTest {

    @Autowired
    private ReferenceDataSeedService referenceDataSeedService;

    @Autowired
    private LocalSampleDataSeedService localSampleDataSeedService;

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
    @DisplayName("기준 데이터와 로컬 샘플 데이터는 멱등하게 생성하고 메뉴 대표 이미지는 생성하지 않는다")
    void initializesSeedDataIdempotentlyWithoutMenuImages() {
        menuItemRepository.save(new MenuItem("BIBIMBAP", "기존 비빔밥", "기존 메뉴 설명"));
        memberRepository.save(Member.builder()
                .loginId("tester01")
                .passwordHash("existing-password-hash")
                .nickname("기존 회원")
                .nicknameCompleted(true)
                .email("existing-tester01@matchuri.test")
                .social(false)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build());

        referenceDataSeedService.initialize();
        presetProfileImageSeedService.initialize();
        localSampleDataSeedService.initialize();
        referenceDataSeedService.initialize();
        presetProfileImageSeedService.initialize();
        localSampleDataSeedService.initialize();

        assertThat(attributeCategoryRepository.count()).isEqualTo(26);
        assertThat(ingredientRepository.count()).isEqualTo(48);
        assertThat(menuItemRepository.count()).isEqualTo(45);
        assertThat(menuAttributeCategoryRepository.count()).isEqualTo(234);
        assertThat(menuIngredientRepository.count()).isEqualTo(130);
        assertThat(menuItemImageRepository.count()).isZero();
        assertThat(imageAssetRepository.count()).isEqualTo(7);
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
        assertThat(memberRepository.findByLoginId("tester01")).get()
                .extracting(Member::getNickname)
                .isEqualTo("기존 회원");
    }
}
