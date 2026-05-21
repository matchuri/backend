package matchuri.backend.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.behavior.entity.ActionType;
import matchuri.backend.domain.behavior.entity.MemberMenuAction;
import matchuri.backend.domain.behavior.repository.MemberMenuActionRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.recommendation.algorithm.MenuRecommendationAlgorithm;
import matchuri.backend.domain.recommendation.algorithm.MenuRecommendationAlgorithmResolver;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.RecommendationTargetType;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.RecommendationContextSnapshot;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationResult;
import matchuri.backend.domain.recommendation.command.GuestPersonalRecommendationCommand;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationRerollType;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import matchuri.backend.domain.recommendation.exception.GuestRecommendationErrorCode;
import matchuri.backend.domain.recommendation.exception.RecommendationErrorCode;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationCandidateRepository;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepository;
import matchuri.backend.domain.recommendation.result.GuestPersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.GuestPersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationSummaryResult;
import matchuri.backend.domain.recommendation.result.SelectPersonalRecommendationResult;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final int RECENT_SELECTED_MENU_EXCLUSION_COUNT = 3;
    private static final int RECOMMENDATION_CANDIDATE_LIMIT = 3;
    private static final long PERSONAL_RECOMMENDATION_OPEN_HOURS = 24;
    private static final String GUEST_PARTICIPANT_KEY = "guest";

    private final ActiveMemberReader activeMemberReader;
    private final PersonalRecommendationRepository personalRecommendationRepository;
    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final PersonalRecommendationCandidateRepository personalRecommendationCandidateRepository;
    private final MemberMenuActionRepository memberMenuActionRepository;
    private final MenuRecommendationAlgorithmResolver menuRecommendationAlgorithmResolver;
    private final ObjectMapper objectMapper;

    /**
     * 현재 로그인한 회원의 취향 프로필과 과거 선택 이력을 기반으로 개인 메뉴 후보를 생성한다.
     *
     * @param contextJson 추천 요청 시점의 컨텍스트 JSON
     * @return 생성된 개인 추천과 추천 후보 목록
     */
    @Override
    @Transactional
    public PersonalRecommendationResult createPersonalRecommendation(String contextJson) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();

        List<PersonalRecommendation> recommendations =
                personalRecommendationRepository.findByMemberIdOrderByRequestedAtDescIdDesc(member.getId());
        closeExpiredOrRejectOpenRecommendation(recommendations);

        return createPersonalRecommendation(member, contextJson, recommendations);
    }

    @Override
    @Transactional
    public PersonalRecommendationResult rerollPersonalRecommendation(
            Long sourcePersonalRecommendationId,
            PersonalRecommendationRerollType rerollType,
            String contextJson
    ) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        PersonalRecommendation sourceRecommendation = getOwnedPersonalRecommendation(sourcePersonalRecommendationId,
                member.getId());

        if (sourceRecommendation.isClosed()) {
            throw new BusinessException(RecommendationErrorCode.ALREADY_CLOSED, sourcePersonalRecommendationId);
        }

        LocalDateTime now = LocalDateTime.now();
        if (isExpiredOpenRecommendation(sourceRecommendation, now)) {
            sourceRecommendation.expire(now);
            throw new BusinessException(RecommendationErrorCode.ALREADY_CLOSED, sourcePersonalRecommendationId);
        }

        if (rerollType == PersonalRecommendationRerollType.NOT_SATISFIED) {
            List<PersonalRecommendationCandidate> candidates =
                    personalRecommendationCandidateRepository.findByPersonalRecommendationIdOrderByRankNoAsc(
                            sourcePersonalRecommendationId);
            List<MemberMenuAction> skipActions = candidates.stream()
                    .map(candidate -> new MemberMenuAction(
                            member,
                            candidate.getMenuItem(),
                            sourceRecommendation,
                            ActionType.SKIP
                    ))
                    .toList();
            memberMenuActionRepository.saveAll(skipActions);
            sourceRecommendation.closeAsRerolledWithSkip(now);
        } else if (rerollType == PersonalRecommendationRerollType.INPUT_CHANGED) {
            sourceRecommendation.closeAsRerolledWithoutSkip(now);
        } else {
            throw new IllegalArgumentException("지원하지 않는 개인 추천 재요청 타입입니다. rerollType=" + rerollType);
        }

        List<PersonalRecommendation> recommendations =
                personalRecommendationRepository.findByMemberIdOrderByRequestedAtDescIdDesc(member.getId());

        return createPersonalRecommendation(member, contextJson, recommendations);
    }

    private PersonalRecommendationResult createPersonalRecommendation(
            Member member,
            String contextJson,
            List<PersonalRecommendation> recommendations
    ) {
        MemberTasteProfile tasteProfile = member.getTasteProfile();

        if (tasteProfile == null) {
            throw new BusinessException(RecommendationErrorCode.TASTE_PROFILE_REQUIRED, member.getId());
        }

        MenuRecommendationAlgorithm algorithm =
                menuRecommendationAlgorithmResolver.resolve(RecommendationAlgorithmType.PERSONAL);

        List<MenuItem> menuItems = menuItemRepository.findAll();
        Map<Long, MenuItem> menuItemById = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, menuItem -> menuItem));

        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.PERSONAL,
                List.of(toTasteProfileSnapshot(member, tasteProfile)),
                toMenuRecommendationProfiles(menuItems),
                RecommendationContextSnapshot.of(contextJson),
                RECOMMENDATION_CANDIDATE_LIMIT,
                findRecentlySelectedMenuIds(recommendations),
                countSelectedAttributeCategoryFrequency(recommendations)
        );
        MenuRecommendationResult recommendationResult = algorithm.recommend(input);

        PersonalRecommendation personalRecommendation = PersonalRecommendation.of(member, contextJson);
        personalRecommendation.markFiltered();
        personalRecommendation.markScored();
        personalRecommendation.complete();
        PersonalRecommendation savedPersonalRecommendation =
                personalRecommendationRepository.save(personalRecommendation);

        List<PersonalRecommendationCandidate> savedCandidates =
                saveRecommendationCandidates(savedPersonalRecommendation, recommendationResult, menuItemById);

        return PersonalRecommendationResult.of(savedPersonalRecommendation, savedCandidates);
    }

    /**
     * 비회원이 요청한 취향 입력을 기반으로 저장 없는 개인 메뉴 후보를 생성한다.
     *
     * @param command 비회원 추천 요청 취향 입력
     * @return 비회원 추천 후보 목록
     */
    @Override
    @Transactional(readOnly = true)
    public GuestPersonalRecommendationResult createGuestPersonalRecommendation(
            GuestPersonalRecommendationCommand command
    ) {
        validateGuestRecommendationCommand(command);

        MenuRecommendationAlgorithm algorithm =
                menuRecommendationAlgorithmResolver.resolve(RecommendationAlgorithmType.GUEST_PERSONAL);
        List<MenuItem> menuItems = menuItemRepository.searchActiveMenuItems(
                null,
                List.of(),
                true,
                List.of(),
                true
        );
        Map<Long, MenuItem> menuItemById = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        MenuRecommendationInput input = new MenuRecommendationInput(
                RecommendationTargetType.GUEST_PERSONAL,
                List.of(toGuestTasteProfileSnapshot(command)),
                toMenuRecommendationProfiles(menuItems),
                RecommendationContextSnapshot.of(command.contextJson()),
                RECOMMENDATION_CANDIDATE_LIMIT,
                List.of(),
                Map.of()
        );

        MenuRecommendationResult recommendationResult = algorithm.recommend(input);

        List<GuestPersonalRecommendationCandidateResult> candidates = recommendationResult.candidates().stream()
                .map(candidate -> {
                    MenuItem menuItem = menuItemById.get(candidate.menuId());

                    return new GuestPersonalRecommendationCandidateResult(
                            candidate.menuId(),
                            menuItem.getName(),
                            candidate.rankNo(),
                            candidate.score()
                    );
                })
                .toList();

        return new GuestPersonalRecommendationResult(candidates);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalRecommendationResult getPersonalRecommendation(Long personalRecommendationId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        PersonalRecommendation personalRecommendation = getOwnedPersonalRecommendation(personalRecommendationId,
                member.getId());
        List<PersonalRecommendationCandidate> candidates =
                personalRecommendationCandidateRepository.findByPersonalRecommendationIdOrderByRankNoAsc(
                        personalRecommendationId);

        return PersonalRecommendationResult.of(personalRecommendation, candidates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalRecommendationCandidateResult> getPersonalRecommendationCandidates(
            Long personalRecommendationId
    ) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        getOwnedPersonalRecommendation(personalRecommendationId, member.getId());

        return personalRecommendationCandidateRepository
                .findByPersonalRecommendationIdOrderByRankNoAsc(personalRecommendationId)
                .stream()
                .map(PersonalRecommendationCandidateResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull PersonalRecommendationSummaryResult> getMyPersonalRecommendations(int page, int size) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();

        return personalRecommendationRepository
                .findByMemberIdOrderByRequestedAtDescIdDesc(member.getId(), PageRequest.of(page, size))
                .map(PersonalRecommendationSummaryResult::from);
    }

    @Override
    @Transactional
    public SelectPersonalRecommendationResult selectPersonalRecommendationCandidate(
            Long personalRecommendationId,
            Long selectedCandidateId
    ) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        PersonalRecommendation personalRecommendation = getOwnedPersonalRecommendation(personalRecommendationId,
                member.getId());

        if (personalRecommendation.isClosed()) {
            throw new BusinessException(RecommendationErrorCode.ALREADY_CLOSED, personalRecommendationId);
        }

        PersonalRecommendationCandidate selectedCandidate = personalRecommendationCandidateRepository
                .findByIdAndPersonalRecommendationId(selectedCandidateId, personalRecommendationId)
                .orElseThrow(() -> new BusinessException(
                        RecommendationErrorCode.CANDIDATE_NOT_FOUND,
                        selectedCandidateId
                ));

        personalRecommendation.select(selectedCandidate, LocalDateTime.now());
        memberMenuActionRepository.save(new MemberMenuAction(
                member,
                selectedCandidate.getMenuItem(),
                personalRecommendation,
                ActionType.CHOOSE
        ));
        personalRecommendationRepository.flush();

        return SelectPersonalRecommendationResult.of(personalRecommendation, selectedCandidate);
    }

    private PersonalRecommendation getOwnedPersonalRecommendation(Long personalRecommendationId, Long memberId) {
        return personalRecommendationRepository.findByIdAndMemberId(personalRecommendationId, memberId)
                .orElseThrow(() -> new BusinessException(RecommendationErrorCode.NOT_FOUND, personalRecommendationId));
    }

    private void closeExpiredOrRejectOpenRecommendation(List<PersonalRecommendation> recommendations) {
        LocalDateTime now = LocalDateTime.now();

        for (PersonalRecommendation recommendation : recommendations) {
            if (!isUnclosedCompletedRecommendation(recommendation)) {
                continue;
            }

            if (!isExpiredOpenRecommendation(recommendation, now)) {
                throw new BusinessException(RecommendationErrorCode.OPEN_EXISTS, recommendation.getId());
            }

            recommendation.expire(now);
        }
    }

    private boolean isUnclosedCompletedRecommendation(PersonalRecommendation recommendation) {
        return recommendation.getStatus() == PersonalRecommendationStatus.COMPLETED
                && recommendation.getSelectedCandidate() == null
                && !recommendation.isClosed();
    }

    private boolean isExpiredOpenRecommendation(PersonalRecommendation recommendation, LocalDateTime now) {
        return isUnclosedCompletedRecommendation(recommendation)
                && !recommendation.getRequestedAt().plusHours(PERSONAL_RECOMMENDATION_OPEN_HOURS).isAfter(now);
    }

    private TasteProfileSnapshot toTasteProfileSnapshot(Member member, MemberTasteProfile tasteProfile) {
        return new TasteProfileSnapshot(
                member.getId(),
                String.valueOf(member.getId()),
                tasteProfile.getPreferAttributeCategories().stream()
                        .map(AttributeCategory::getId)
                        .toList(),
                tasteProfile.getRestrictionIngredients().stream()
                        .map(Ingredient::getId)
                        .toList(),
                tasteProfile.getDisLikeMenuItems().stream()
                        .map(MenuItem::getId)
                        .toList()
        );
    }

    private TasteProfileSnapshot toGuestTasteProfileSnapshot(GuestPersonalRecommendationCommand command) {
        return new TasteProfileSnapshot(
                null,
                GUEST_PARTICIPANT_KEY,
                command.attributeCategoryIds(),
                command.restrictionIngredientIds(),
                command.dislikedMenuItemIds()
        );
    }

    private List<MenuRecommendationProfile> toMenuRecommendationProfiles(List<MenuItem> menuItems) {
        Map<Long, List<Long>> ingredientIdsByMenuId = menuIngredientRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        menuIngredient -> menuIngredient.getMenu().getId(),
                        Collectors.mapping(menuIngredient -> menuIngredient.getIngredient().getId(),
                                Collectors.toList())
                ));

        return menuItems.stream()
                .map(menuItem -> new MenuRecommendationProfile(
                        menuItem.getId(),
                        menuItem.getCode(),
                        menuItem.getName(),
                        menuItem.getMenuAttributeCategories().stream()
                                .map(MenuAttributeCategory::getAttributeCategory)
                                .map(AttributeCategory::getId)
                                .toList(),
                        ingredientIdsByMenuId.getOrDefault(menuItem.getId(), List.of())
                ))
                .toList();
    }

    private void validateGuestRecommendationCommand(GuestPersonalRecommendationCommand command) {
        validateNoDuplicateIds(
                command.attributeCategoryIds(),
                GuestRecommendationErrorCode.DUPLICATE_ATTRIBUTE_CATEGORY
        );
        validateNoDuplicateIds(
                command.restrictionIngredientIds(),
                GuestRecommendationErrorCode.DUPLICATE_RESTRICTION_INGREDIENT
        );
        validateNoDuplicateIds(
                command.dislikedMenuItemIds(),
                GuestRecommendationErrorCode.DUPLICATE_DISLIKED_MENU_ITEM
        );

        validateActiveAttributeCategoryIds(command.attributeCategoryIds());
        validateActiveRestrictionIngredientIds(command.restrictionIngredientIds());
        validateActiveDislikedMenuItemIds(command.dislikedMenuItemIds());
    }

    private void validateNoDuplicateIds(List<Long> ids, GuestRecommendationErrorCode errorCode) {
        if (ids.size() == new LinkedHashSet<>(ids).size()) {
            return;
        }

        throw new BusinessException(errorCode, ids);
    }

    private void validateActiveAttributeCategoryIds(List<Long> attributeCategoryIds) {
        if (attributeCategoryIds.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(GuestRecommendationErrorCode.INVALID_ATTRIBUTE_CATEGORY, attributeCategoryIds);
        }

        List<AttributeCategory> attributeCategories = attributeCategoryRepository.findAllByIdInAndActiveTrue(
                attributeCategoryIds);
        if (attributeCategories.size() != attributeCategoryIds.size()) {
            throw new BusinessException(GuestRecommendationErrorCode.INVALID_ATTRIBUTE_CATEGORY, attributeCategoryIds);
        }
    }

    private void validateActiveRestrictionIngredientIds(List<Long> restrictionIngredientIds) {
        if (restrictionIngredientIds.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(
                    GuestRecommendationErrorCode.INVALID_RESTRICTION_INGREDIENT,
                    restrictionIngredientIds
            );
        }

        List<Ingredient> ingredients = ingredientRepository.findAllByIdInAndActiveTrue(restrictionIngredientIds);
        if (ingredients.size() != restrictionIngredientIds.size()) {
            throw new BusinessException(
                    GuestRecommendationErrorCode.INVALID_RESTRICTION_INGREDIENT,
                    restrictionIngredientIds
            );
        }
    }

    private void validateActiveDislikedMenuItemIds(List<Long> dislikedMenuItemIds) {
        if (dislikedMenuItemIds.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(GuestRecommendationErrorCode.INVALID_DISLIKED_MENU_ITEM, dislikedMenuItemIds);
        }

        List<MenuItem> menuItems = menuItemRepository.findAllByIdInAndActiveTrue(dislikedMenuItemIds);
        if (menuItems.size() != dislikedMenuItemIds.size()) {
            throw new BusinessException(GuestRecommendationErrorCode.INVALID_DISLIKED_MENU_ITEM, dislikedMenuItemIds);
        }
    }

    private List<Long> findRecentlySelectedMenuIds(List<PersonalRecommendation> recommendations) {
        return recommendations.stream()
                .filter(recommendation -> recommendation.getSelectedCandidate() != null)
                .map(PersonalRecommendation::getSelectedMenu)
                .map(MenuItem::getId)
                .limit(RECENT_SELECTED_MENU_EXCLUSION_COUNT)
                .toList();
    }

    /**
     * 최종 추천 후보를 개인 추천 엔티티에 연결해 저장한다.
     *
     * @param savedPersonalRecommendation 저장된 개인 추천 엔티티
     * @param recommendationResult 점수 계산이 끝난 추천 결과
     * @param menuItemById 메뉴 ID별 메뉴 엔티티 map
     * @return 저장된 개인 추천 후보 목록
     */
    private List<PersonalRecommendationCandidate> saveRecommendationCandidates(
            PersonalRecommendation savedPersonalRecommendation,
            MenuRecommendationResult recommendationResult,
            Map<Long, MenuItem> menuItemById
    ) {
        List<PersonalRecommendationCandidate> personalRecommendationCandidates = recommendationResult.candidates()
                .stream()
                .map(candidate -> PersonalRecommendationCandidate.of(
                            savedPersonalRecommendation,
                            menuItemById.get(candidate.menuId()),
                            candidate.rankNo(),
                            candidate.score(),
                            toCandidateMetaJson(recommendationResult, candidate)
                    ))
                .toList();

        return personalRecommendationCandidateRepository.saveAll(personalRecommendationCandidates);
    }

    private String toCandidateMetaJson(
            MenuRecommendationResult recommendationResult,
            MenuRecommendationCandidateResult candidate
    ) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("algorithmType", recommendationResult.algorithmType().name());
        meta.put("algorithmVersion", recommendationResult.algorithmVersion());
        meta.put("scoreBreakdown", candidate.scoreBreakdown());
        meta.put("candidateMeta", candidate.meta());

        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("개인 추천 후보 메타 정보를 JSON으로 변환할 수 없습니다.", exception);
        }
    }

    private Map<Long, Long> countSelectedAttributeCategoryFrequency(
            List<PersonalRecommendation> recommendations
    ) {
        return recommendations.stream()
                .filter(recommendation -> recommendation.getSelectedCandidate() != null)
                .map(PersonalRecommendation::getSelectedMenuAttributeCategory)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(AttributeCategory::getId, LinkedHashMap::new, Collectors.counting()));
    }
}
