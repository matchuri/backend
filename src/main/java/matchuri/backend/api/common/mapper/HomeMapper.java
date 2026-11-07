package matchuri.backend.api.common.mapper;

import java.util.List;
import matchuri.backend.api.common.dto.response.HomeResponse;
import matchuri.backend.domain.group.result.GroupHomeActivityResult;
import matchuri.backend.domain.member.result.MemberLocationResult;
import matchuri.backend.domain.member.result.MemberProfileResult;
import matchuri.backend.domain.member.result.MemberTasteProfileSummaryResult;
import matchuri.backend.domain.member.result.MemberTasteProfileSummaryResult.AttributeCategoryItem;
import matchuri.backend.domain.menu.result.MenuAttributeCategoryResult;
import matchuri.backend.domain.recommendation.result.PersonalRecommendationHomeResult;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class HomeMapper {
    public HomeResponse toResponse(
            MemberProfileResult user,
            @Nullable MemberLocationResult location,
            MemberTasteProfileSummaryResult taste,
            PersonalRecommendationHomeResult recommendations,
            List<GroupHomeActivityResult> activities
    ) {
        var latest = recommendations.latestRecommendation();
        return new HomeResponse(
                new HomeResponse.UserCard(user.nickname(), user.profileImageUrl()),
                new HomeResponse.PersonalRecommendationSection(
                        latest == null ? null : latest.id(), latest == null ? null : latest.status()),
                location == null ? null : new HomeResponse.LocationCard(
                        location.longitude(), location.latitude(), location.address()),
                new HomeResponse.TasteProfileCard(taste.attributeCategories().stream().map(this::category).toList()),
                new HomeResponse.PersonalRecommendationHistorySection(
                        recommendations.selectedRecommendations().stream()
                                .map(item -> new HomeResponse.PersonalRecommendationHistoryItem(item.id(), item.createdAt(),
                                        new HomeResponse.SelectedMenu(item.menuName(),
                                                item.attributeCategories().stream().map(this::category).toList())))
                                .toList()),
                new HomeResponse.GroupActivitySection(activities.stream()
                        .map(item -> new HomeResponse.GroupActivityItem(item.groupId(), item.groupName(), item.type(),
                                new HomeResponse.GroupActivityDetails(item.recommendationId(), item.createdAt(),
                                        item.startedAt(), item.endedAt(), item.selectedMenuName())))
                        .toList()));
    }

    private HomeResponse.AttributeCategory category(AttributeCategoryItem item) {
        return new HomeResponse.AttributeCategory(item.id(), item.categoryType(), item.code(), item.name(), item.sortOrder());
    }

    private HomeResponse.AttributeCategory category(MenuAttributeCategoryResult item) {
        return new HomeResponse.AttributeCategory(item.id(), item.categoryType(), item.code(), item.name(), item.sortOrder());
    }
}
