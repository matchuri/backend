package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record CreateMemberResponse(
        @Schema(description = "생성된 회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "생성된 계정의 loginId입니다.", example = "tester01")
        String loginId,

        @Schema(description = "회원 생성 시각입니다.", example = "2026-04-07T10:15:30")
        LocalDateTime createdAt
) {
}
