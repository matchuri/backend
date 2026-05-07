package matchuri.backend.domain.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.repository.MemberTasteProfileCategoryRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileDislikedMenuItemRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRestrictionIngredientRepository;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationCandidateRepository;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepository;
import matchuri.backend.domain.recommendation.support.MenuItemScoreBoard;
import matchuri.backend.domain.recommendation.support.ScoreCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final MemberTasteProfileRepository memberTasteProfileRepository;
    private final MemberTasteProfileCategoryRepository memberTasteProfileCategoryRepository;
    private final MemberTasteProfileRestrictionIngredientRepository memberTasteProfileRestrictionIngredientRepository;
    private final MemberTasteProfileDislikedMenuItemRepository memberTasteProfileDislikedMenuItemRepository;
    private final ActiveMemberReader activeMemberReader;
    private final PersonalRecommendationRepository personalRecommendationRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final PersonalRecommendationCandidateRepository personalRecommendationCandidateRepository;


    @Override
    public void getPersonalRecommendation(String contextJson) {
        // 취향 프로필, 지난 내역 불러오기
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        MemberTasteProfile tasteProfile = member.getTasteProfile();

        // 알레르기 식품과 비선호 식품은 완전히 배제
        // 알레르기 식재료
        List<Ingredient> restrictionIngredients = tasteProfile.getRestrictionIngredients();
        List<MenuItem> menuItemExceptByIngredients = findMenuItemsExceptByIngredients(restrictionIngredients);

        // 비선호 음식
        List<MenuItem> disLikeMenuItems = tasteProfile.getDisLikeMenuItems();
        List<MenuItem> menuItemsExceptByMenuItems = findMenuItemsExceptByMenuItems(disLikeMenuItems);

        // 추천 가능한 메뉴 (1차 필터링)
        Set<Long> menuItemIdsExceptByMenuItems = menuItemsExceptByMenuItems.stream()
                .map(MenuItem::getId)
                .collect(Collectors.toSet());

        Map<Long, MenuItem> availableMenuItemsMap = menuItemExceptByIngredients.stream()
                .filter(menuItem -> menuItemIdsExceptByMenuItems.contains(menuItem.getId()))
                .collect(Collectors.toMap(MenuItem::getId, menuItem -> menuItem));

        // 이전 요청 목록
        List<PersonalRecommendation> recommendations = personalRecommendationRepository.findByMemberId(member.getId());
        // 이전 요청 목록에서 선택했던 메뉴
        List<MenuItem> selectedMenuItems = recommendations.stream()
                .map(PersonalRecommendation::getSelectedMenu)
                .toList();

        // 최근에 선택했던 메뉴들을 제외 (2차 필터링)
        int n = 3;
        List<MenuItem> latestSelectedMenus = selectedMenuItems.subList(0, Math.min(n, selectedMenuItems.size()));
        latestSelectedMenus
                .forEach(menuItem -> {
                    availableMenuItemsMap.remove(menuItem.getId());
                });

        // 필터링을 마친 예비 후보들
        Map<Long, MenuItemScoreBoard> menuItemScoreBoardMap = availableMenuItemsMap.values().stream()
                .map(MenuItemScoreBoard::of)
                .collect(Collectors.toMap(
                        menuItemScoreBoard -> menuItemScoreBoard.getMenuItem().getId(),
                        menuItemScoreBoard -> menuItemScoreBoard
                ));

        // 선호 카테고리 종합
        List<AttributeCategory> preferAttributeCategories = tasteProfile.getPreferAttributeCategories();

        // score1: 필터링된 메뉴 리스트에서 선호 카테고리가 일치하는 갯수 넣기
        menuItemScoreBoardMap.forEach((id, menuItemScoreBoard) -> {
            MenuItem menuItem = menuItemScoreBoard.getMenuItem();
            long matchingCount = menuItem.countMatchingCategories(preferAttributeCategories);

            menuItemScoreBoard.addCategoryMatchingCount(matchingCount);
        });

        // 이전에 선택했던 메뉴들의 카테고리를 종합하여 가중치 부여
        List<List<AttributeCategory>> selectedMenuCategoryGroups = new ArrayList<>();

        recommendations.stream()
                .map(PersonalRecommendation::getSelectedMenuAttributeCategory)
                .forEach(selectedMenuCategoryGroups::add);

        // score2: 이전에 선택했던 메뉴들의 카테고리 빈도 수
        Map<AttributeCategory, Long> categoryFrequencyMap = countCategoryFrequency(selectedMenuCategoryGroups);

        menuItemScoreBoardMap.forEach((id, menuItemScoreBoard) -> {
            menuItemScoreBoard.setWeightMatchingCount(categoryFrequencyMap);
        });

        // 계산 후 최종 MenuItems
        ScoreCalculator scoreCalculator = ScoreCalculator.of(preferAttributeCategories, categoryFrequencyMap);
        List<MenuItemScoreBoard> finalizeMenuItems = scoreCalculator.calculate(menuItemScoreBoardMap);

        // 요청 엔티티, 후보 엔티티 생성
        PersonalRecommendation personalRecommendation = PersonalRecommendation.of(member, contextJson);
        PersonalRecommendation savedPersonalRecommendation =
                personalRecommendationRepository.save(personalRecommendation);

        List<PersonalRecommendationCandidate> personalRecommendationCandidates = IntStream.range(0,
                        finalizeMenuItems.size())
                .mapToObj(i -> {
                    MenuItemScoreBoard board = finalizeMenuItems.get(i);

                    return PersonalRecommendationCandidate.of(
                            savedPersonalRecommendation,
                            board.getMenuItem(),
                            i,
                            board.getTotalScore()
                    );
                })
                .toList();

        List<PersonalRecommendationCandidate> savedPersonalRecommendationCandidates =
                personalRecommendationCandidateRepository.saveAll(personalRecommendationCandidates);

    }

    public List<MenuItem> findMenuItemsExceptByMenuItems(List<MenuItem> menuItems) {
        List<Long> ids = menuItems.stream()
                .map(MenuItem::getId)
                .toList();

        if (ids.isEmpty()) {
            return menuItemRepository.findAll();
        }

        return menuItemRepository.findAllByIdNotIn(ids);
    }

    public List<MenuItem> findMenuItemsExceptByIngredients(List<Ingredient> ingredients) {
        List<Long> ids = ingredients.stream()
                .map(Ingredient::getId)
                .toList();

        List<MenuIngredient> allByIngredientIdNotIn = menuIngredientRepository.findAllByIngredientIdNotIn(ids);

        return allByIngredientIdNotIn.stream()
                .map(MenuIngredient::getMenu)
                .toList();
    }

    public Map<AttributeCategory, Long> countCategoryFrequency(
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
