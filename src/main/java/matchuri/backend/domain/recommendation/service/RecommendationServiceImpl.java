package matchuri.backend.domain.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.exception.RecommendationErrorCode;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationCandidateRepository;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepository;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationSummaryResult;
import matchuri.backend.domain.recommendation.result.SelectPersonalRecommendationResult;
import matchuri.backend.domain.recommendation.support.MenuItemScoreBoard;
import matchuri.backend.domain.recommendation.support.ScoreCalculator;
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

    private final ActiveMemberReader activeMemberReader;
    private final PersonalRecommendationRepository personalRecommendationRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final PersonalRecommendationCandidateRepository personalRecommendationCandidateRepository;
    private final MemberMenuActionRepository memberMenuActionRepository;

    /**
     * 현재 로그인한 회원의 취향 프로필과 과거 선택 이력을 기반으로 개인 메뉴 후보를 생성한다.
     *
     * @param contextJson 추천 요청 시점의 컨텍스트 JSON
     * @return 생성된 개인 추천과 추천 후보 목록
     */
    @Override
    public PersonalRecommendationResult createPersonalRecommendation(String contextJson) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        MemberTasteProfile tasteProfile = member.getTasteProfile();

        if (tasteProfile == null) {
            throw new BusinessException(RecommendationErrorCode.TASTE_PROFILE_REQUIRED, member.getId());
        }

        List<PersonalRecommendation> recommendations =
                personalRecommendationRepository.findByMemberIdOrderByRequestedAtDescIdDesc(member.getId());

        Map<Long, MenuItem> availableMenuItemsMap = findAvailableMenuItems(tasteProfile);
        excludeRecentlySelectedMenus(availableMenuItemsMap, recommendations);

        Map<Long, MenuItemScoreBoard> menuItemScoreBoardMap = availableMenuItemsMap.values().stream()
                .map(MenuItemScoreBoard::of)
                .collect(Collectors.toMap(
                        menuItemScoreBoard -> menuItemScoreBoard.getMenuItem().getId(),
                        menuItemScoreBoard -> menuItemScoreBoard
                ));

        List<AttributeCategory> preferAttributeCategories = tasteProfile.getPreferAttributeCategories();
        addCategoryMatchingCounts(menuItemScoreBoardMap, preferAttributeCategories);

        Map<AttributeCategory, Long> categoryFrequencyMap = countSelectedCategoryFrequency(recommendations);
        addHistoryWeightCounts(menuItemScoreBoardMap, categoryFrequencyMap);

        ScoreCalculator scoreCalculator = ScoreCalculator.of(preferAttributeCategories, categoryFrequencyMap);
        List<MenuItemScoreBoard> finalizeMenuItems = scoreCalculator.calculate(menuItemScoreBoardMap);

        PersonalRecommendation personalRecommendation = PersonalRecommendation.of(member, contextJson);
        personalRecommendation.markFiltered();
        personalRecommendation.markScored();
        personalRecommendation.complete();
        PersonalRecommendation savedPersonalRecommendation =
                personalRecommendationRepository.save(personalRecommendation);

        List<PersonalRecommendationCandidate> savedCandidates =
                saveRecommendationCandidates(savedPersonalRecommendation, finalizeMenuItems);

        return PersonalRecommendationResult.of(savedPersonalRecommendation, savedCandidates);
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
    public SelectPersonalRecommendationResult selectPersonalRecommendationCandidate(
            Long personalRecommendationId,
            Long selectedCandidateId
    ) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        PersonalRecommendation personalRecommendation = getOwnedPersonalRecommendation(personalRecommendationId,
                member.getId());

        if (personalRecommendation.getSelectedCandidate() != null) {
            throw new BusinessException(RecommendationErrorCode.ALREADY_SELECTED, personalRecommendationId);
        }

        PersonalRecommendationCandidate selectedCandidate = personalRecommendationCandidateRepository
                .findByIdAndPersonalRecommendationId(selectedCandidateId, personalRecommendationId)
                .orElseThrow(() -> new BusinessException(
                        RecommendationErrorCode.CANDIDATE_NOT_FOUND,
                        selectedCandidateId
                ));

        personalRecommendation.select(selectedCandidate);
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

    /**
     * 제한 식재료와 비선호 메뉴를 제외한 추천 가능 메뉴 목록을 조회한다.
     *
     * @param tasteProfile 회원 취향 프로필
     * @return 추천 가능 메뉴 ID를 key로 하는 메뉴 map
     */
    private Map<Long, MenuItem> findAvailableMenuItems(MemberTasteProfile tasteProfile) {
        List<MenuItem> menuItemsExceptByIngredients =
                findMenuItemsExceptByIngredients(tasteProfile.getRestrictionIngredients());
        List<MenuItem> menuItemsExceptByMenuItems =
                findMenuItemsExceptByMenuItems(tasteProfile.getDisLikeMenuItems());

        Set<Long> menuItemIdsExceptByMenuItems = menuItemsExceptByMenuItems.stream()
                .map(MenuItem::getId)
                .collect(Collectors.toSet());

        return menuItemsExceptByIngredients.stream()
                .filter(menuItem -> menuItemIdsExceptByMenuItems.contains(menuItem.getId()))
                .collect(Collectors.toMap(
                        MenuItem::getId,
                        menuItem -> menuItem,
                        (current, ignored) -> current
                ));
    }

    /**
     * 과거 추천에서 최근 선택한 메뉴를 현재 후보 목록에서 제거한다.
     *
     * @param availableMenuItemsMap 추천 후보 메뉴 map
     * @param recommendations 회원의 과거 개인 추천 목록
     */
    private void excludeRecentlySelectedMenus(
            Map<Long, MenuItem> availableMenuItemsMap,
            List<PersonalRecommendation> recommendations
    ) {
        recommendations.stream()
                .filter(recommendation -> recommendation.getSelectedCandidate() != null)
                .map(PersonalRecommendation::getSelectedMenu)
                .limit(RECENT_SELECTED_MENU_EXCLUSION_COUNT)
                .forEach(menuItem -> availableMenuItemsMap.remove(menuItem.getId()));
    }

    /**
     * 후보 메뉴별로 회원이 명시적으로 선호한 attribute category와 일치하는 개수를 반영한다.
     *
     * @param menuItemScoreBoardMap 후보 메뉴별 점수판 map
     * @param preferAttributeCategories 회원이 선호한 attribute category 목록
     */
    private void addCategoryMatchingCounts(
            Map<Long, MenuItemScoreBoard> menuItemScoreBoardMap,
            List<AttributeCategory> preferAttributeCategories
    ) {
        menuItemScoreBoardMap.values()
                .forEach(menuItemScoreBoard -> {
                    MenuItem menuItem = menuItemScoreBoard.getMenuItem();
                    long matchingCount = menuItem.countMatchingCategories(preferAttributeCategories);

                    menuItemScoreBoard.addCategoryMatchingCount(matchingCount);
                });
    }

    /**
     * 과거 선택 메뉴에서 자주 등장한 attribute category 빈도를 후보 메뉴 점수판에 반영한다.
     *
     * @param menuItemScoreBoardMap 후보 메뉴별 점수판 map
     * @param categoryFrequencyMap 과거 선택 메뉴의 attribute category 빈도 map
     */
    private void addHistoryWeightCounts(
            Map<Long, MenuItemScoreBoard> menuItemScoreBoardMap,
            Map<AttributeCategory, Long> categoryFrequencyMap
    ) {
        menuItemScoreBoardMap.values()
                .forEach(menuItemScoreBoard -> menuItemScoreBoard.setWeightMatchingCount(categoryFrequencyMap));
    }

    /**
     * 최종 추천 후보를 개인 추천 엔티티에 연결해 저장한다.
     *
     * @param savedPersonalRecommendation 저장된 개인 추천 엔티티
     * @param finalizeMenuItems 점수 계산이 끝난 최종 후보 목록
     * @return 저장된 개인 추천 후보 목록
     */
    private List<PersonalRecommendationCandidate> saveRecommendationCandidates(
            PersonalRecommendation savedPersonalRecommendation,
            List<MenuItemScoreBoard> finalizeMenuItems
    ) {
        List<PersonalRecommendationCandidate> personalRecommendationCandidates = IntStream.range(0,
                        finalizeMenuItems.size())
                .mapToObj(i -> {
                    MenuItemScoreBoard board = finalizeMenuItems.get(i);

                    return PersonalRecommendationCandidate.of(
                            savedPersonalRecommendation,
                            board.getMenuItem(),
                            i + 1,
                            board.getTotalScore()
                    );
                })
                .toList();

        return personalRecommendationCandidateRepository.saveAll(personalRecommendationCandidates);
    }

    /**
     * 지정한 비선호 메뉴를 제외한 메뉴 목록을 조회한다.
     *
     * @param menuItems 제외할 메뉴 목록
     * @return 비선호 메뉴가 제외된 메뉴 목록
     */
    private List<MenuItem> findMenuItemsExceptByMenuItems(List<MenuItem> menuItems) {
        List<Long> ids = menuItems.stream()
                .map(MenuItem::getId)
                .toList();

        if (ids.isEmpty()) {
            return menuItemRepository.findAll();
        }

        return menuItemRepository.findAllByIdNotIn(ids);
    }

    /**
     * 지정한 제한 식재료를 제외한 메뉴 목록을 조회한다.
     *
     * @param ingredients 제외할 제한 식재료 목록
     * @return 제한 식재료가 제외된 메뉴 목록
     */
    private List<MenuItem> findMenuItemsExceptByIngredients(List<Ingredient> ingredients) {
        List<Long> ids = ingredients.stream()
                .map(Ingredient::getId)
                .toList();

        if (ids.isEmpty()) {
            return menuItemRepository.findAll();
        }

        List<MenuIngredient> allByIngredientIdNotIn = menuIngredientRepository.findAllByIngredientIdNotIn(ids);

        return allByIngredientIdNotIn.stream()
                .map(MenuIngredient::getMenu)
                .distinct()
                .toList();
    }

    /**
     * 과거 개인 추천에서 선택된 메뉴들의 attribute category 빈도를 계산한다.
     *
     * @param recommendations 회원의 과거 개인 추천 목록
     * @return 선택 이력 기반 attribute category 빈도 map
     */
    private Map<AttributeCategory, Long> countSelectedCategoryFrequency(
            List<PersonalRecommendation> recommendations
    ) {
        List<List<AttributeCategory>> selectedMenuCategoryGroups = new ArrayList<>();

        recommendations.stream()
                .filter(recommendation -> recommendation.getSelectedCandidate() != null)
                .map(PersonalRecommendation::getSelectedMenuAttributeCategory)
                .forEach(selectedMenuCategoryGroups::add);

        return countCategoryFrequency(selectedMenuCategoryGroups);
    }

    /**
     * attribute category 목록들을 하나로 펼쳐 category별 등장 횟수를 계산한다.
     *
     * @param categoryLists attribute category 목록들의 그룹
     * @return attribute category별 등장 횟수 map
     */
    private Map<AttributeCategory, Long> countCategoryFrequency(
            List<List<AttributeCategory>> categoryLists
    ) {
        Map<Long, AttributeCategory> categoryById = new LinkedHashMap<>();
        Map<Long, Long> countById = new LinkedHashMap<>();

        categoryLists.stream()
                .flatMap(List::stream)
                .forEach(category -> {
                    Long id = category.getId();
                    categoryById.putIfAbsent(id, category);
                    countById.merge(id, 1L, Long::sum);
                });

        return countById.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> categoryById.get(entry.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
