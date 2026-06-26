package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupMyVoteResponse(
        @Schema(description = "현재 로그인한 회원의 투표 여부입니다.", example = "true")
        boolean voted,

        @Schema(description = "현재 로그인한 회원이 투표한 후보 ID입니다. 투표하지 않았으면 null입니다.", example = "8001")
        Long candidateId
) {
}
