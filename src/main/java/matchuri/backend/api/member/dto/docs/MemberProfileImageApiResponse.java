package matchuri.backend.api.member.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.api.member.dto.response.MemberProfileImageResponse;

@Schema(description = "회원 프로필 이미지 설정 API 공통 응답")
public record MemberProfileImageApiResponse(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "회원 프로필 이미지 설정 결과")
        MemberProfileImageResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보")
        ErrorResponse error
) {
}
