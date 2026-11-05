package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationDetailResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "그룹 추천 상태입니다.", example = "OPEN")
        GroupRecommendationStatus status,

        @Schema(description = "추천 당시 위치 등 컨텍스트 JSON 문자열입니다.", nullable = true)
        String contextJson,

        @Schema(description = "준비 단계 진행률입니다. PREPARING 상태가 아니면 null입니다.", nullable = true)
        GroupRecommendationReadinessProgressResponse readiness,

        @Schema(description = "추천 후보 목록입니다.")
        List<GroupRecommendationCandidateResponse> candidates,

        @Schema(description = "추천 시점의 카테고리입니다. 후보 생성 전에는 null, 생성 후에는 최대 5개입니다. 카테고리 이름은 현재 값을 반환합니다.", nullable = true)
        List<GroupRecommendationCategoryResponse> recommendationCategories,

        @Schema(description = "투표 진행률입니다. PREPARING 상태이면 null입니다.", nullable = true)
        GroupVoteProgressResponse voteProgress,

        @Schema(description = "현재 활성 그룹원별 투표 여부입니다. PREPARING 상태이면 빈 배열입니다.")
        List<GroupMemberVoteResponse> memberVotes,

        @Schema(description = "최종 확정 후보입니다. 확정 전에는 null입니다.", nullable = true)
        GroupRecommendationCandidateResponse finalCandidate,

        @Schema(description = "그룹 추천 생성 시각입니다.", example = "2026-05-06T12:05:00")
        LocalDateTime createdAt
) {
}
