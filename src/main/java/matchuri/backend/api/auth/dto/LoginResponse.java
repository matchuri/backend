package matchuri.backend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(
                description = "보호 API 호출에 사용하는 Matchuri access token입니다. 프론트는 이후 `Authorization: Bearer <token>` 헤더에 이 값을 사용합니다.",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        String accessToken,

        @Schema(
                description = "현재 단계에서는 응답 body에 refresh token을 내려주지 않으므로 항상 null입니다. 실제 refresh token은 HttpOnly 쿠키로 처리됩니다.",
                nullable = true,
                example = "null"
        )
        String refreshToken,

        @Schema(
                description = "access token 만료까지 남은 시간(초)입니다.",
                example = "3600"
        )
        long expiresIn,

        @Schema(description = "로그인한 회원의 최소 요약 정보입니다.")
        LoginMemberSummary member
) {

    public record LoginMemberSummary(
            @Schema(description = "로그인한 회원 ID입니다.", example = "1")
            Long id,

            @Schema(description = "로그인한 회원 역할입니다.", example = "MEMBER")
            String role
    ) {
    }
}
