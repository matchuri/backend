package matchuri.backend.api.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import matchuri.backend.api.member.dto.docs.RegisterLocalMemberApiResponse;
import matchuri.backend.api.member.dto.request.RegisterLocalMemberV2Request;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Member", description = "회원 관련 공개/인증 API")
public interface MemberApiV2 {

    @Operation(
            summary = "취향 프로필 포함 자체 회원가입",
            description = """
                    회원 기본 정보, 검증된 이메일, 필수 약관 동의, 초기 취향 프로필을 하나의 요청으로 원자적으로 저장합니다.

                    - 가입 성공 시 자동 로그인되지 않습니다. 이후 로그인 시 취향 프로필까지 완료된 온보딩 상태를 반환합니다.
                    - `tasteProfile`과 세 ID 목록은 필수이며 각 목록은 비어 있을 수 있습니다.
                    - 취향 ID는 활성 참조 데이터만 허용하며 같은 목록 안의 중복 ID는 거절합니다.
                    - 처리 중 하나라도 실패하면 회원, 약관 동의, 취향 프로필을 모두 롤백합니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "취향 프로필 포함 회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterLocalMemberApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "memberId": 1,
                                                "loginId": "tester01",
                                                "email": "tester@example.com",
                                                "nickname": "점심탐험가",
                                                "createdAt": "2026-08-19T12:00:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 필드 오류, 중복 취향 ID 또는 유효하지 않은 취향 ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "invalidTasteAttributeCategory",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 400,
                                                "code": "MEMBER_INVALID_TASTE_ATTRIBUTE_CATEGORY",
                                                "message": "유효하지 않거나 비활성화된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : [999]",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "이메일 인증 token이 없거나, 만료되었거나, 요청 이메일과 맞지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 loginId, nickname, email 또는 유효하지 않은 약관 버전"
            )
    })
    ApiResponse<RegisterLocalMemberResponse> registerLocalMember(RegisterLocalMemberV2Request request);
}
