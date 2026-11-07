package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupVoteProgressResponse(
        @Schema(description = "투표 대상 멤버 수입니다.", example = "4")
        Integer totalMemberCount,

        @Schema(description = "투표를 완료한 멤버 수입니다.", example = "3")
        Integer votedMemberCount
) {
    public static GroupVoteProgressResponse mockInProgress() {
        return new GroupVoteProgressResponse(4, 3);
    }
}
