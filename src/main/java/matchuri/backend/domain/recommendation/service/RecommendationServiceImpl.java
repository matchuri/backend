package matchuri.backend.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
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
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.exception.RecommendationErrorCode;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationCandidateRepository;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepository;
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

    private final ActiveMemberReader activeMemberReader;
    private final PersonalRecommendationRepository personalRecommendationRepository;
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
    public PersonalRecommendationResult createPersonalRecommendation(String contextJson) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        MemberTasteProfile tasteProfile = member.getTasteProfile();

        if (tasteProfile == null) {
            throw new BusinessException(RecommendationErrorCode.TASTE_PROFILE_REQUIRED, member.getId());
        }

        List<PersonalRecommendation> recommendations =
                personalRecommendationRepository.findByMemberIdOrderByRequestedAtDescIdDesc(member.getId());

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
                3,
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
