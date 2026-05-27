package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationSessionResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "그룹 추천 상태입니다.", example = "OPEN")
        GroupRecommendationStatus status,

        @Schema(description = "준비 단계 진행률입니다. PREPARING 상태가 아니면 null입니다.", nullable = true)
        GroupRecommendationReadinessProgressResponse readiness,

        @Schema(description = "추천 후보 목록입니다.")
        List<GroupRecommendationCandidateResponse> candidates,

        @Schema(description = "투표 진행률입니다. PREPARING 상태이면 null입니다.", nullable = true)
        GroupVoteProgressResponse voteProgress,

        @Schema(description = "최종 확정 후보입니다. 확정 전에는 null입니다.")
        GroupRecommendationCandidateResponse finalCandidate,

        @Schema(description = "그룹 추천 생성 시각입니다.", example = "2026-05-06T12:05:00")
        LocalDateTime createdAt
) {
    public static GroupRecommendationSessionResponse mockOpen() {
        return new GroupRecommendationSessionResponse(
                5001L,
                GroupRecommendationStatus.OPEN,
                null,
                GroupMocks.candidates(),
                GroupVoteProgressResponse.mockInProgress(),
                null,
                LocalDateTime.of(2026, 5, 6, 12, 5)
        );
    }
}
