package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record RegisterLocalMemberResponse(
        @Schema(description = "생성된 회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "가입 완료된 loginId입니다.", example = "tester01")
        String loginId,

        @Schema(description = "가입 시 인증 완료된 이메일입니다.", example = "tester@example.com")
        String email,

        @Schema(description = "가입 시 확정된 닉네임입니다.", example = "점심탐험가")
        String nickname,

        @Schema(description = "회원 가입 완료 시각입니다.", example = "2026-04-14T20:15:30")
        LocalDateTime createdAt
) {
}
