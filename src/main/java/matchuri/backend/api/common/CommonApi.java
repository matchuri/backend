package matchuri.backend.api.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import matchuri.backend.api.common.docs.ErrorExamples;
import matchuri.backend.api.common.dto.docs.HomeApiResponse;
import matchuri.backend.api.common.dto.response.HomeResponse;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;

@Tag(name = "Common", description = "여러 도메인을 조합한 공통 화면 API")
public interface CommonApi {
    @Operation(summary = "홈 조회", description = """
            인증된 회원의 홈 컴포넌트 데이터를 제공합니다. 실제 서비스/DB 조회 API입니다.
            - 필수 약관과 닉네임 온보딩을 완료한 활성 회원만 사용할 수 있습니다.
            - 개인 기록은 SELECTED만 요청 시각 DESC, ID DESC로 최대 3건 반환합니다. 페이징 입력은 없습니다.
            - 메뉴명과 활성 메뉴 속성 카테고리는 현재 마스터 기준이며 메뉴 이미지 URL은 제공하지 않습니다.
            - 취향 칩은 선택한 카테고리 전체이며 제한 재료/비선호 메뉴는 제외합니다.
            - 그룹 활동은 활성 가입 중이고 삭제되지 않은 모든 그룹의 추천 세션 히스토리입니다.
              같은 그룹의 여러 추천이 포함될 수 있으며, 상태별 활동 시각 DESC, ID DESC로 정렬합니다.
            - 그룹 type은 추천 세션의 현재 상태입니다. 같은 세션의 상태 전환을 각각 쌓는 이벤트 로그가 아닙니다.
            - PREPARING은 createdAt, OPEN은 startedAt, 종료 상태는 endedAt을 화면 활동 시각으로 사용합니다.
            - 만료된 개인/그룹 추천은 기존 24시간 lazy expiration 정책을 반영합니다.
            """)
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "홈 조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = HomeApiResponse.class),
                        examples = @ExampleObject(name = "success", value = """
                                {"success":true,"data":{
                                  "user":{"nickname":"점심탐험가","profileImageUrl":null},
                                  "personalRecommendation":{"latestRecommendationId":9002,"latestRecommendationStatus":"OPEN"},
                                  "location":{"longitude":127.027610,"latitude":37.498095,"address":"서울 서초구 서초동"},
                                  "tasteProfile":{"attributeCategories":[{"id":1,"categoryType":"FLAVOR","code":"SPICY","name":"매콤","sortOrder":10}]},
                                  "personalRecommendationHistory":{"items":[{"id":9001,"createdAt":"2026-08-28T12:00:00","selectedMenu":{"name":"김치찌개","attributeCategories":[{"id":2,"categoryType":"FOOD_CATEGORY","code":"KOREAN","name":"한식","sortOrder":10}]}}]},
                                  "recentGroupActivities":{"items":[{"groupId":3001,"groupName":"점심팟","type":"FINALIZED","details":{"recommendationId":5001,"createdAt":"2026-08-29T12:00:00","startedAt":"2026-08-29T12:05:00","endedAt":"2026-08-29T12:10:00","selectedMenuName":"마라탕"}}]}
                                },"error":null}
                                """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 또는 유효하지 않은 토큰",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = HomeApiResponse.class), examples = {
                        @ExampleObject(name = "missing", value = ErrorExamples.AUTH_TOKEN_MISSING),
                        @ExampleObject(name = "invalid", value = ErrorExamples.AUTH_TOKEN_INVALID)})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "필수 온보딩 미완료 또는 비활성 회원",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = HomeApiResponse.class), examples = {
                        @ExampleObject(name = "agreements", value = ErrorExamples.MEMBER_AGREEMENT_REQUIRED),
                        @ExampleObject(name = "nickname", value = ErrorExamples.MEMBER_NICKNAME_REQUIRED),
                        @ExampleObject(name = "inactive", value = ErrorExamples.MEMBER_INACTIVE)}))
    })
    ApiResponse<HomeResponse> home(@Parameter(hidden = true) @AuthenticatedMemberId Long memberId);
}
