package matchuri.backend.infra.seed;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.group.entity.GroupLocation;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.repository.GroupLocationRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileCategoryRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileDislikedMenuItemRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRestrictionIngredientRepository;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalSampleDataSeedService {

    private static final String RESOURCE_PATH = "seed/local-sample-data.json";

    private final SeedDataResourceLoader resourceLoader;
    private final MemberRepository memberRepository;
    private final MemberAgreementRepository memberAgreementRepository;
    private final MemberTasteProfileRepository memberTasteProfileRepository;
    private final MemberTasteProfileCategoryRepository memberTasteProfileCategoryRepository;
    private final MemberTasteProfileRestrictionIngredientRepository restrictionIngredientRepository;
    private final MemberTasteProfileDislikedMenuItemRepository dislikedMenuItemRepository;
    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemRepository menuItemRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupLocationRepository groupLocationRepository;

    @Transactional
    public void initialize() {
        LocalSampleSeedData seedData = resourceLoader.load(RESOURCE_PATH, LocalSampleSeedData.class);
        Map<String, Member> members = seedMembers(seedData);
        seedAgreements(seedData, members);
        seedTasteProfiles(seedData, members);
        seedGroups(seedData, members);

        log.info(
                "Local sample seed initialization completed. members={}, tasteProfiles={}, groups={}",
                seedData.members().size(),
                seedData.tasteProfiles().size(),
                seedData.groups().size()
        );
    }

    private Map<String, Member> seedMembers(LocalSampleSeedData seedData) {
        Map<String, Member> members = new HashMap<>();
        memberRepository.findAll().stream()
                .filter(member -> member.getLoginId() != null)
                .forEach(member -> members.put(member.getLoginId(), member));

        for (LocalSampleSeedData.MemberSeed seed : seedData.members()) {
            if (members.containsKey(seed.loginId())) {
                continue;
            }

            Member member = Member.builder()
                    .loginId(seed.loginId())
                    .passwordHash(seed.passwordHash())
                    .nickname(seed.nickname())
                    .nicknameCompleted(true)
                    .email(seed.email())
                    .social(false)
                    .memberRole(seed.role())
                    .status(MemberStatus.ACTIVE)
                    .build();
            members.put(seed.loginId(), memberRepository.save(member));
        }
        return members;
    }

    private void seedAgreements(LocalSampleSeedData seedData, Map<String, Member> members) {
        for (Member member : membersFor(seedData, members)) {
            for (LocalSampleSeedData.AgreementSeed agreement : seedData.agreements()) {
                if (!memberAgreementRepository.existsByMemberIdAndAgreementTypeAndAgreementVersion(
                        member.getId(),
                        agreement.type(),
                        agreement.version()
                )) {
                    memberAgreementRepository.save(MemberAgreement.create(
                            member,
                            agreement.type(),
                            agreement.version()
                    ));
                }
            }
        }
    }

    private void seedTasteProfiles(LocalSampleSeedData seedData, Map<String, Member> members) {
        Map<AttributeCategoryKey, AttributeCategory> categories = new HashMap<>();
        attributeCategoryRepository.findAll().forEach(category -> categories.put(
                new AttributeCategoryKey(category.getCategoryType(), category.getCode()),
                category
        ));
        Map<String, Ingredient> ingredients = new HashMap<>();
        ingredientRepository.findAll().forEach(ingredient -> ingredients.put(ingredient.getCode(), ingredient));
        Map<String, MenuItem> menuItems = new HashMap<>();
        menuItemRepository.findAll().forEach(menuItem -> menuItems.put(menuItem.getCode(), menuItem));

        for (LocalSampleSeedData.TasteProfileSeed seed : seedData.tasteProfiles()) {
            Member member = require(members, seed.loginId(), "member");
            MemberTasteProfile profile = memberTasteProfileRepository.findByMemberId(member.getId())
                    .orElseGet(() -> memberTasteProfileRepository.save(
                            new MemberTasteProfile(member, seed.profileVersion())
                    ));

            Set<Long> categoryIds = memberTasteProfileCategoryRepository.findAllByProfileId(profile.getId()).stream()
                    .map(mapping -> mapping.getAttributeCategory().getId())
                    .collect(HashSet::new, Set::add, Set::addAll);
            for (LocalSampleSeedData.AttributeCategoryRef categoryRef : seed.preferredAttributeCategories()) {
                AttributeCategory category = require(
                        categories,
                        new AttributeCategoryKey(categoryRef.categoryType(), categoryRef.code()),
                        "attribute category"
                );
                if (categoryIds.add(category.getId())) {
                    memberTasteProfileCategoryRepository.save(new MemberTasteProfileCategory(profile, category));
                }
            }

            Set<Long> ingredientIds = restrictionIngredientRepository.findAllByProfileId(profile.getId()).stream()
                    .map(mapping -> mapping.getIngredient().getId())
                    .collect(HashSet::new, Set::add, Set::addAll);
            for (String ingredientCode : seed.restrictionIngredientCodes()) {
                Ingredient ingredient = require(ingredients, ingredientCode, "ingredient");
                if (ingredientIds.add(ingredient.getId())) {
                    restrictionIngredientRepository.save(
                            new MemberTasteProfileRestrictionIngredient(profile, ingredient)
                    );
                }
            }

            Set<Long> dislikedMenuIds = dislikedMenuItemRepository.findAllByProfileId(profile.getId()).stream()
                    .map(mapping -> mapping.getMenuItem().getId())
                    .collect(HashSet::new, Set::add, Set::addAll);
            for (String menuCode : seed.dislikedMenuCodes()) {
                MenuItem menuItem = require(menuItems, menuCode, "menu item");
                if (dislikedMenuIds.add(menuItem.getId())) {
                    dislikedMenuItemRepository.save(new MemberTasteProfileDislikedMenuItem(profile, menuItem));
                }
            }
        }
    }

    private void seedGroups(LocalSampleSeedData seedData, Map<String, Member> members) {
        for (LocalSampleSeedData.GroupSeed seed : seedData.groups()) {
            Member host = require(members, seed.hostLoginId(), "group host member");
            GroupRoom room = groupRoomRepository.findByInviteCode(seed.inviteCode())
                    .orElseGet(() -> groupRoomRepository.saveAndFlush(
                            GroupRoom.createOwnedBy(seed.name(), seed.inviteCode(), host)
                    ));

            for (LocalSampleSeedData.GroupMemberSeed memberSeed : seed.members()) {
                Member member = require(members, memberSeed.loginId(), "group member");
                if (groupRoomMemberRepository.findByRoomIdAndMemberId(room.getId(), member.getId()).isEmpty()) {
                    groupRoomMemberRepository.save(new GroupRoomMember(
                            room,
                            member,
                            memberSeed.role(),
                            LocalDateTime.now()
                    ));
                }
            }

            if (groupLocationRepository.findFirstByRoomIdOrderByCreatedAtDescIdDesc(room.getId()).isEmpty()) {
                LocalSampleSeedData.LocationSeed location = seed.location();
                groupLocationRepository.save(new GroupLocation(
                        room,
                        location.latitude(),
                        location.longitude(),
                        location.radiusMeters(),
                        location.address()
                ));
            }
        }
    }

    private Set<Member> membersFor(LocalSampleSeedData seedData, Map<String, Member> members) {
        Set<Member> result = new HashSet<>();
        seedData.members().forEach(seed -> result.add(require(members, seed.loginId(), "member")));
        return result;
    }

    private <K, V> V require(Map<K, V> values, K key, String target) {
        V value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("Local sample seed가 참조하는 " + target + "을 찾을 수 없습니다. key=" + key);
        }
        return value;
    }

    private record AttributeCategoryKey(CategoryType categoryType, String code) {
    }
}
