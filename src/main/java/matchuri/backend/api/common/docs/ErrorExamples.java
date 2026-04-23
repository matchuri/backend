package matchuri.backend.api.common.docs;

public final class ErrorExamples {

    public static final String AUTH_TOKEN_MISSING = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 401,
                "code": "AUTH_TOKEN_MISSING",
                "message": "인증 토큰이 필요합니다.",
                "details": []
              }
            }
            """;

    public static final String AUTH_TOKEN_INVALID = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 401,
                "code": "AUTH_TOKEN_INVALID",
                "message": "유효하지 않은 인증 토큰입니다.",
                "details": []
              }
            }
            """;

    public static final String AUTH_TOKEN_EXPIRED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 401,
                "code": "AUTH_TOKEN_EXPIRED",
                "message": "만료된 인증 토큰입니다.",
                "details": []
              }
            }
            """;

    public static final String AUTH_REFRESH_TOKEN_MISSING = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 401,
                "code": "AUTH_REFRESH_TOKEN_MISSING",
                "message": "리프레시 토큰이 필요합니다.",
                "details": []
              }
            }
            """;

    public static final String AUTH_REFRESH_TOKEN_INVALID = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 401,
                "code": "AUTH_REFRESH_TOKEN_INVALID",
                "message": "유효하지 않은 리프레시 토큰입니다.",
                "details": []
              }
            }
            """;

    public static final String AUTH_REFRESH_TOKEN_EXPIRED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 401,
                "code": "AUTH_REFRESH_TOKEN_EXPIRED",
                "message": "만료된 리프레시 토큰입니다.",
                "details": []
              }
            }
            """;

    public static final String AUTH_FORBIDDEN = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 403,
                "code": "AUTH_FORBIDDEN",
                "message": "접근 권한이 없습니다.",
                "details": []
              }
            }
            """;

    public static final String MEMBER_INACTIVE = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 403,
                "code": "MEMBER_INACTIVE_MEMBER",
                "message": "비활성화된 회원입니다. memberId : 1",
                "details": []
              }
            }
            """;

    public static final String MEMBER_AGREEMENT_REQUIRED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 403,
                "code": "MEMBER_AGREEMENT_REQUIRED",
                "message": "필수 약관 동의가 필요합니다.",
                "details": []
              }
            }
            """;

    public static final String MEMBER_NICKNAME_REQUIRED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 403,
                "code": "MEMBER_NICKNAME_REQUIRED",
                "message": "닉네임 설정이 필요합니다.",
                "details": []
              }
            }
            """;

    public static final String COMMON_INVALID_BODY_FIELD = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 400,
                "code": "COMMON_INVALID_BODY_FIELD",
                "message": "요청 바디 필드가 올바르지 않습니다.",
                "details": [
                  {
                    "source": "BODY",
                    "field": "attributeCategoryIds",
                    "reason": "attributeCategoryIds는 null일 수 없습니다."
                  }
                ]
              }
            }
            """;

    private ErrorExamples() {
    }
}
