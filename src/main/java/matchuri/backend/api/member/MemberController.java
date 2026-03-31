package matchuri.backend.api.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.member.dto.LoginIdExistsResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.global.exception.RequestValidationException;
import matchuri.backend.domain.member.service.MemberService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 관련 공개/인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Operation(
            summary = "로그인 ID 중복 확인",
            description = """
                    회원 가입 시 사용할 로그인 ID가 이미 사용 중인지 확인합니다.
                    이 API는 인증 없이 호출할 수 있습니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "정상 조회",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginIdExistsResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "loginId": "tester01",
                                                "exists": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "로그인 ID 형식이 올바르지 않은 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "invalidLoginId",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 400,
                                                "code": "COMMON_INVALID_PATH_VARIABLE",
                                                "message": "경로 변수 형식이 올바르지 않습니다",
                                                "details": [
                                                  {
                                                    "source": "PATH",
                                                    "field": "loginId",
                                                    "reason": "로그인 아이디는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/exists/{loginId}")
    public ApiResponse<LoginIdExistsResponse> checkLoginIdExists(
            @Parameter(
                    description = """
                            회원 가입 시 사용할 로그인 ID입니다.
                            필수 path variable입니다.
                            제약:
                            - 1자 이상 50자 이하
                            - 공백 불가
                            - 허용 문자: 영문 대소문자, 숫자, 점(.), 밑줄(_), 하이픈(-)
                            - 서버 정규식 검증: ^[A-Za-z0-9._-]+$
                            허용 예시: tester01, matchuri-user, user.name
                            비허용 예시: test user, 한글아이디, 50자 초과 문자열
                            """,
                    example = "tester01"
            )
            @PathVariable String loginId
    ) {
        validateLoginId(loginId);
        boolean exists = memberService.existsByLoginId(loginId);
        return ApiResponse.success(memberMapper.toLoginIdExistsResponse(loginId, exists));
    }

    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw RequestValidationException.invalidPathVariable("loginId", "로그인 아이디는 비어 있을 수 없습니다.");
        }

        if (loginId.length() > Member.LOGIN_ID_MAX_SIZE) {
            throw RequestValidationException.invalidPathVariable("loginId", "로그인 아이디는 50자를 초과할 수 없습니다.");
        }

        if (!loginId.matches(Member.LOGIN_ID_PATTERN)) {
            throw RequestValidationException.invalidPathVariable(
                    "loginId",
                    "로그인 아이디는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다."
            );
        }
    }
}
