package matchuri.backend.api.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import org.jspecify.annotations.Nullable;

@Schema(description = "홈 컴포넌트별 데이터입니다. 목록은 항상 배열이며 메뉴 이미지 URL은 포함하지 않습니다.")
public record HomeResponse(
        UserCard user,
        PersonalRecommendationSection personalRecommendation,
        @Schema(description = "저장된 검색 기준 위치입니다. 미설정이면 null입니다.", nullable = true)
        @Nullable LocationCard location,
        TasteProfileCard tasteProfile,
        PersonalRecommendationHistorySection personalRecommendationHistory,
        GroupActivitySection recentGroupActivities
) {
    @Schema(name = "HomeUserCard")
    public record UserCard(String nickname, @Nullable String profileImageUrl) {}

    @Schema(name = "HomePersonalRecommendationSection")
    public record PersonalRecommendationSection(
            @Schema(description = "상태와 무관한 최신 개인 추천 ID. 이력이 없으면 null입니다.", nullable = true)
            @Nullable Long latestRecommendationId,
            @Schema(description = "최신 추천 상태. 만료가 반영되며, 이력이 없으면 null입니다.", nullable = true)
            @Nullable PersonalRecommendationStatus latestRecommendationStatus
    ) {}

    @Schema(name = "HomeLocationCard")
    public record LocationCard(BigDecimal longitude, BigDecimal latitude, String address) {}

    @Schema(name = "HomeTasteProfileCard")
    public record TasteProfileCard(List<AttributeCategory> attributeCategories) {}

    @Schema(name = "HomeAttributeCategory")
    public record AttributeCategory(Long id, CategoryType categoryType, String code, String name, int sortOrder) {}

    @Schema(name = "HomePersonalRecommendationHistorySection")
    public record PersonalRecommendationHistorySection(List<PersonalRecommendationHistoryItem> items) {}

    @Schema(name = "HomePersonalRecommendationHistoryItem")
    public record PersonalRecommendationHistoryItem(
            Long id,
            @Schema(description = "추천 요청 시각(requestedAt)입니다. 선택 완료 시각이 아닙니다.")
            LocalDateTime createdAt,
            SelectedMenu selectedMenu
    ) {}

    @Schema(name = "HomeSelectedMenu")
    public record SelectedMenu(String name, List<AttributeCategory> attributeCategories) {}

    @Schema(name = "HomeGroupActivitySection")
    public record GroupActivitySection(List<GroupActivityItem> items) {}

    @Schema(name = "HomeGroupActivityItem")
    public record GroupActivityItem(
            Long groupId,
            String groupName,
            @Schema(description = "그룹 최신 추천 상태이며 색상·아이콘 분기 기준입니다. 이벤트 타입이 아닙니다.")
            GroupRecommendationStatus type,
            GroupActivityDetails details
    ) {}

    @Schema(name = "HomeGroupActivityDetails")
    public record GroupActivityDetails(
            Long recommendationId,
            @Schema(description = "추천 세션 생성 시각입니다.")
            LocalDateTime createdAt,
            @Schema(description = "투표 시작 시각입니다. PREPARING이면 null입니다.", nullable = true)
            @Nullable LocalDateTime startedAt,
            @Schema(description = "종료 시각. PREPARING/OPEN이면 null, lazy 만료 시에는 만료 처리 시각입니다.", nullable = true)
            @Nullable LocalDateTime endedAt,
            @Schema(description = "FINALIZED일 때만 최신 확정 메뉴명을 제공합니다. 나머지 상태는 null입니다.", nullable = true)
            @Nullable String selectedMenuName
    ) {}
}
