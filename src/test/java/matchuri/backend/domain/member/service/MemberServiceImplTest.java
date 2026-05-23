package matchuri.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import matchuri.backend.domain.auth.support.verification.EmailVerificationTokenVerifier;
import matchuri.backend.domain.member.command.CreateMemberCommand;
import matchuri.backend.domain.member.command.RegisterLocalMemberCommand;
import matchuri.backend.domain.member.command.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.command.UpdateMemberBasicInfoCommand;
import matchuri.backend.domain.member.command.UpdateMemberTasteProfileCommand;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileCategoryRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileDislikedMenuItemRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRestrictionIngredientRepository;
import matchuri.backend.domain.member.result.MemberProfileResult;
import matchuri.backend.domain.member.result.MemberTasteProfileSummaryResult;
import matchuri.backend.domain.member.result.MemberTasteUpdateResult;
import matchuri.backend.domain.member.result.OnboardingNextStep;
import matchuri.backend.domain.member.result.OnboardingStatusResult;
import matchuri.backend.domain.member.result.RegisterLocalMemberResult;
import matchuri.backend.domain.member.result.UpdateMemberResult;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRequestValidator;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.domain.member.support.onboarding.OnboardingStatusResolver;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepository;
import matchuri.backend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberAgreementRepository memberAgreementRepository;

    @Mock
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Mock
    private MemberTasteProfileCategoryRepository memberTasteProfileCategoryRepository;

    @Mock
    private MemberTasteProfileRestrictionIngredientRepository memberTasteProfileRestrictionIngredientRepository;

    @Mock
    private MemberTasteProfileDislikedMenuItemRepository memberTasteProfileDislikedMenuItemRepository;

    @Mock
    private AttributeCategoryRepository attributeCategoryRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RequiredAgreementRequestValidator requiredAgreementRequestValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ActiveMemberReader activeMemberReader;

    @Mock
    private OnboardingStatusResolver onboardingStatusResolver;

    @Mock
    private EmailVerificationTokenVerifier emailVerificationTokenVerifier;

    @Mock
    private PersonalRecommendationRepository personalRecommendationRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("자체 회원가입 통합은 회원과 필수 약관을 함께 저장한다")
    void registerLocalMemberSavesMemberAndRequiredAgreements() {
        RegisterLocalMemberCommand command = new RegisterLocalMemberCommand(
                "tester01",
                "P@ssw0rd!",
                "점심탐험가",
                "tester@example.com",
                "ev_signup-token",
                List.of(
                        new SubmitRequiredAgreementsCommand.AgreementConsentCommand("TERMS_OF_SERVICE", "2026-04-10"),
                        new SubmitRequiredAgreementsCommand.AgreementConsentCommand("PRIVACY_POLICY", "2026-04-10")
                )
        );

        Member savedMember = Member.builder()
                .id(1L)
                .loginId("tester01")
                .passwordHash("encoded-password")
                .email("tester@example.com")
                .nickname("점심탐험가")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(memberRepository.existsByNickname("점심탐험가")).thenReturn(false);
        when(memberRepository.existsByEmailAndSocialFalseAndStatus("tester@example.com", MemberStatus.ACTIVE))
                .thenReturn(false);
        when(passwordEncoder.encode("P@ssw0rd!")).thenReturn("encoded-password");
        when(memberRepository.saveAndFlush(any(Member.class))).thenReturn(savedMember);
        when(requiredAgreementRequestValidator.validateAndIndex(any())).thenReturn(Map.of(
                AgreementType.TERMS_OF_SERVICE, "2026-04-10",
                AgreementType.PRIVACY_POLICY, "2026-04-10"
        ));

        RegisterLocalMemberResult result = memberService.registerLocalMember(command);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.loginId()).isEqualTo("tester01");
        assertThat(result.email()).isEqualTo("tester@example.com");
        assertThat(result.nickname()).isEqualTo("점심탐험가");
        verify(emailVerificationTokenVerifier).verifySignupToken("tester@example.com", "ev_signup-token");
        verify(memberRepository).saveAndFlush(any(Member.class));
        verify(requiredAgreementRequestValidator).validateAndIndex(command.agreements());
        verify(memberAgreementRepository, times(2)).save(any(MemberAgreement.class));
    }

    @Test
    @DisplayName("회원 가입 저장 충돌은 MEMBER_DUPLICATE_LOGIN_ID로 번역한다")
    void createMemberTranslatesIntegrityViolationToDuplicateLoginId() {
        CreateMemberCommand command = new CreateMemberCommand("tester01", "P@ssw0rd!");

        when(memberRepository.existsByLoginId("tester01")).thenReturn(false);
        when(passwordEncoder.encode("P@ssw0rd!")).thenReturn("encoded-password");
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate login id"));

        assertThatThrownBy(() -> memberService.createMember(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    @DisplayName("내 닉네임 수정 시 이미 사용 중인 닉네임이면 MEMBER_DUPLICATE_NICKNAME을 반환한다")
    void updateMyProfileRejectsDuplicateNickname() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .nickname("현재닉네임")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(memberRepository.existsByNickname("중복닉네임")).thenReturn(true);

        assertThatThrownBy(() -> memberService.updateMyProfile(new UpdateMemberBasicInfoCommand("중복닉네임")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("내 닉네임 수정 시 같은 닉네임이면 중복 검사 없이 유지한다")
    void updateMyProfileAllowsSameNickname() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .nickname("현재닉네임")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(onboardingStatusResolver.resolve(member))
                .thenReturn(new OnboardingStatusResult(true, true, true, OnboardingNextStep.READY));

        UpdateMemberResult result = memberService.updateMyProfile(new UpdateMemberBasicInfoCommand("현재닉네임"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.onboarding().nextStep()).isEqualTo(OnboardingNextStep.READY);
        assertThat(member.getNickname()).isEqualTo("현재닉네임");
        verify(memberRepository).flush();
    }

    @Test
    @DisplayName("내 프로필 조회는 현재 회원의 loginId를 함께 반환한다")
    void getMyProfileReturnsLoginId() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .nickname("점심탐험가")
                .email("tester@example.com")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);

        MemberProfileResult result = memberService.getMyProfile();

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.loginId()).isEqualTo("tester01");
        assertThat(result.nickname()).isEqualTo("점심탐험가");
        assertThat(result.isSocial()).isFalse();
        assertThat(result.email()).isEqualTo("tester@example.com");
    }

    @Test
    @DisplayName("내 취향 프로필 조회 시 프로필이 없으면 빈 배열 기반 응답을 반환한다")
    void getMyTasteProfileReturnsEmptyProfileWhenProfileDoesNotExist() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(memberTasteProfileRepository.findByMemberId(1L)).thenReturn(java.util.Optional.empty());

        MemberTasteProfileSummaryResult result = memberService.getMyTasteProfile();

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.profileVersion()).isEqualTo(MemberTasteProfileSummaryResult.DEFAULT_PROFILE_VERSION);
        assertThat(result.attributeCategories()).isEmpty();
        assertThat(result.restrictionIngredients()).isEmpty();
        assertThat(result.dislikedMenuItems()).isEmpty();
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    @DisplayName("내 취향 프로필 조회 시 저장된 선택 항목을 표시용 메타데이터와 함께 반환한다")
    void getMyTasteProfileReturnsSelectedItems() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();
        MemberTasteProfile profile = new MemberTasteProfile(member, "v2");
        AttributeCategory attributeCategory = new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10);
        Ingredient ingredient = new Ingredient("PEANUT", "땅콩", true, 10);
        MenuItem menuItem = new MenuItem("PORK_CUTLET", "돈까스", "바삭한 돼지고기 튀김");

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(memberTasteProfileRepository.findByMemberId(1L)).thenReturn(java.util.Optional.of(profile));
        when(memberTasteProfileCategoryRepository.findAllByProfileIdOrderByDisplay(profile.getId()))
                .thenReturn(List.of(new MemberTasteProfileCategory(profile, attributeCategory)));
        when(memberTasteProfileRestrictionIngredientRepository.findAllByProfileIdOrderByDisplay(profile.getId()))
                .thenReturn(List.of(new MemberTasteProfileRestrictionIngredient(profile, ingredient)));
        when(memberTasteProfileDislikedMenuItemRepository.findAllByProfileIdOrderByDisplay(profile.getId()))
                .thenReturn(List.of(new MemberTasteProfileDislikedMenuItem(profile, menuItem)));

        MemberTasteProfileSummaryResult result = memberService.getMyTasteProfile();

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.profileVersion()).isEqualTo("v2");
        assertThat(result.attributeCategories()).singleElement()
                .extracting(
                        MemberTasteProfileSummaryResult.AttributeCategoryItem::categoryType,
                        MemberTasteProfileSummaryResult.AttributeCategoryItem::code,
                        MemberTasteProfileSummaryResult.AttributeCategoryItem::name
                )
                .containsExactly(CategoryType.FLAVOR, "SPICY", "매운맛");
        assertThat(result.restrictionIngredients()).singleElement()
                .extracting(
                        MemberTasteProfileSummaryResult.RestrictionIngredientItem::code,
                        MemberTasteProfileSummaryResult.RestrictionIngredientItem::name,
                        MemberTasteProfileSummaryResult.RestrictionIngredientItem::allergen
                )
                .containsExactly("PEANUT", "땅콩", true);
        assertThat(result.dislikedMenuItems()).singleElement()
                .extracting(
                        MemberTasteProfileSummaryResult.DislikedMenuItem::code,
                        MemberTasteProfileSummaryResult.DislikedMenuItem::name
                )
                .containsExactly("PORK_CUTLET", "돈까스");
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 프로필이 없어도 헤더와 매핑을 함께 생성한다")
    void updateMyTasteProfileCreatesProfileAndMappings() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();
        AttributeCategory attributeCategory = new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10);
        Ingredient ingredient = new Ingredient("PEANUT", "땅콩", true, 10);
        MenuItem menuItem = new MenuItem("PORK_CUTLET", "돈까스", "바삭한 돼지고기 튀김");
        MemberTasteProfile savedProfile = new MemberTasteProfile(member, "v1");

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(memberTasteProfileRepository.findByMemberId(1L)).thenReturn(Optional.empty());
        when(memberTasteProfileRepository.saveAndFlush(any(MemberTasteProfile.class))).thenReturn(savedProfile);
        when(attributeCategoryRepository.findAllByIdInAndActiveTrue(List.of(1L))).thenReturn(
                List.of(attributeCategory));
        when(ingredientRepository.findAllByIdInAndActiveTrue(List.of(101L))).thenReturn(List.of(ingredient));
        when(menuItemRepository.findAllByIdInAndActiveTrue(List.of(1001L))).thenReturn(List.of(menuItem));
        when(memberTasteProfileCategoryRepository.findAllByProfileId(savedProfile.getId())).thenReturn(
                Collections.emptyList());
        when(memberTasteProfileRestrictionIngredientRepository.findAllByProfileId(savedProfile.getId())).thenReturn(
                Collections.emptyList());
        when(memberTasteProfileDislikedMenuItemRepository.findAllByProfileId(savedProfile.getId())).thenReturn(
                Collections.emptyList());
        when(memberTasteProfileCategoryRepository.findAllByProfileIdOrderByDisplay(savedProfile.getId()))
                .thenReturn(List.of(new MemberTasteProfileCategory(savedProfile, attributeCategory)));
        when(memberTasteProfileRestrictionIngredientRepository.findAllByProfileIdOrderByDisplay(savedProfile.getId()))
                .thenReturn(List.of(new MemberTasteProfileRestrictionIngredient(savedProfile, ingredient)));
        when(memberTasteProfileDislikedMenuItemRepository.findAllByProfileIdOrderByDisplay(savedProfile.getId()))
                .thenReturn(List.of(new MemberTasteProfileDislikedMenuItem(savedProfile, menuItem)));

        MemberTasteUpdateResult result = memberService.updateMyTasteProfile(
                new UpdateMemberTasteProfileCommand(List.of(1L), List.of(101L), List.of(1001L))
        );
        MemberTasteProfileSummaryResult profile = result.profile();

        assertThat(profile.profileVersion()).isEqualTo("v1");
        assertThat(profile.attributeCategories()).hasSize(1);
        assertThat(profile.restrictionIngredients()).hasSize(1);
        assertThat(profile.dislikedMenuItems()).hasSize(1);
        assertThat(result.openPersonalRecommendationId()).isNull();
        verify(memberTasteProfileRepository).saveAndFlush(any(MemberTasteProfile.class));
        verify(memberTasteProfileCategoryRepository).saveAll(any());
        verify(memberTasteProfileRestrictionIngredientRepository).saveAll(any());
        verify(memberTasteProfileDislikedMenuItemRepository).saveAll(any());
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 미종료 개인 추천이 있으면 재요청용 추천 ID를 함께 반환한다")
    void updateMyTasteProfileReturnsOpenPersonalRecommendationId() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();
        MemberTasteProfile profile = new MemberTasteProfile(member, "v1");
        PersonalRecommendation openRecommendation = mock(PersonalRecommendation.class);

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(memberTasteProfileRepository.findByMemberId(1L)).thenReturn(Optional.of(profile));
        when(attributeCategoryRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(ingredientRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(menuItemRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(memberTasteProfileCategoryRepository.findAllByProfileId(profile.getId())).thenReturn(List.of());
        when(memberTasteProfileRestrictionIngredientRepository.findAllByProfileId(profile.getId())).thenReturn(List.of());
        when(memberTasteProfileDislikedMenuItemRepository.findAllByProfileId(profile.getId())).thenReturn(List.of());
        when(memberTasteProfileCategoryRepository.findAllByProfileIdOrderByDisplay(profile.getId())).thenReturn(List.of());
        when(memberTasteProfileRestrictionIngredientRepository.findAllByProfileIdOrderByDisplay(profile.getId()))
                .thenReturn(List.of());
        when(memberTasteProfileDislikedMenuItemRepository.findAllByProfileIdOrderByDisplay(profile.getId()))
                .thenReturn(List.of());
        when(personalRecommendationRepository
                .findFirstByMemberIdAndStatusAndSelectedCandidateIsNullAndClosedAtIsNullOrderByRequestedAtDescIdDesc(
                        1L,
                        PersonalRecommendationStatus.OPEN
                ))
                .thenReturn(Optional.of(openRecommendation));
        when(openRecommendation.getId()).thenReturn(9001L);

        MemberTasteUpdateResult result = memberService.updateMyTasteProfile(
                new UpdateMemberTasteProfileCommand(List.of(), List.of(), List.of())
        );

        assertThat(result.openPersonalRecommendationId()).isEqualTo(9001L);
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 중복 attribute category ID를 거절한다")
    void updateMyTasteProfileRejectsDuplicateAttributeCategoryIds() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);

        assertThatThrownBy(() -> memberService.updateMyTasteProfile(
                new UpdateMemberTasteProfileCommand(List.of(1L, 1L), List.of(), List.of())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_TASTE_ATTRIBUTE_CATEGORY);
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 중복 disliked menu item ID를 거절한다")
    void updateMyTasteProfileRejectsDuplicateDislikedMenuItemIds() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);

        assertThatThrownBy(() -> memberService.updateMyTasteProfile(
                new UpdateMemberTasteProfileCommand(List.of(), List.of(), List.of(1001L, 1001L))
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_TASTE_DISLIKED_MENU_ITEM);
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 비활성 또는 존재하지 않는 restriction ingredient ID를 거절한다")
    void updateMyTasteProfileRejectsInvalidRestrictionIngredientIds() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(attributeCategoryRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(ingredientRepository.findAllByIdInAndActiveTrue(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> memberService.updateMyTasteProfile(
                new UpdateMemberTasteProfileCommand(List.of(), List.of(999L), List.of())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_TASTE_RESTRICTION_INGREDIENT);
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 비활성 또는 존재하지 않는 disliked menu item ID를 거절한다")
    void updateMyTasteProfileRejectsInvalidDislikedMenuItemIds() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(attributeCategoryRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(ingredientRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(menuItemRepository.findAllByIdInAndActiveTrue(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> memberService.updateMyTasteProfile(
                new UpdateMemberTasteProfileCommand(List.of(), List.of(), List.of(999L))
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_TASTE_DISLIKED_MENU_ITEM);
    }

    @Test
    @DisplayName("내 취향 프로필 저장은 빈 배열로 전체 비우기를 허용한다")
    void updateMyTasteProfileAllowsEmptyLists() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();
        MemberTasteProfile profile = new MemberTasteProfile(member, "v1");

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(memberTasteProfileRepository.findByMemberId(1L)).thenReturn(Optional.of(profile));
        when(attributeCategoryRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(ingredientRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(menuItemRepository.findAllByIdInAndActiveTrue(List.of())).thenReturn(List.of());
        when(memberTasteProfileCategoryRepository.findAllByProfileId(profile.getId())).thenReturn(
                Collections.emptyList());
        when(memberTasteProfileRestrictionIngredientRepository.findAllByProfileId(profile.getId())).thenReturn(
                Collections.emptyList());
        when(memberTasteProfileDislikedMenuItemRepository.findAllByProfileId(profile.getId())).thenReturn(
                Collections.emptyList());
        when(memberTasteProfileCategoryRepository.findAllByProfileIdOrderByDisplay(profile.getId())).thenReturn(
                List.of());
        when(memberTasteProfileRestrictionIngredientRepository.findAllByProfileIdOrderByDisplay(
                profile.getId())).thenReturn(List.of());
        when(memberTasteProfileDislikedMenuItemRepository.findAllByProfileIdOrderByDisplay(profile.getId())).thenReturn(
                List.of());

        MemberTasteUpdateResult result = memberService.updateMyTasteProfile(
                new UpdateMemberTasteProfileCommand(List.of(), List.of(), List.of())
        );
        MemberTasteProfileSummaryResult profileResult = result.profile();

        assertThat(profileResult.profileVersion()).isEqualTo("v1");
        assertThat(profileResult.attributeCategories()).isEmpty();
        assertThat(profileResult.restrictionIngredients()).isEmpty();
        assertThat(profileResult.dislikedMenuItems()).isEmpty();
        assertThat(result.openPersonalRecommendationId()).isNull();
    }
}
