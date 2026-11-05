package matchuri.backend.api.recommendation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import matchuri.backend.api.recommendation.dto.docs.PersonalRecommendationHistoryPageApiResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationHistoryResponse;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;

public interface RecommendationV2Api {

    @Operation(
            summary = "내 개인 추천 이력 목록 조회 v2",
            description = """
                    현재 로그인한 회원의 개인 추천 이력을 최신 요청 순서로 반환합니다.

                    각 이력의 대표 메뉴는 최종 선택 후보가 있으면 선택 후보, 없으면 1순위 후보입니다.
                    대표 후보가 없는 실패 이력은 score, menuName, thumbnailUrl이 null이고 tags는 빈 목록입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonalRecommendationHistoryPageApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "content": [
                                                  {
                                                    "id": 9001,
                                                    "status": "OPEN",
                                                    "requestedAt": "2026-05-06T12:10:00",
                                                    "closedAt": null,
                                                    "score": 93.5,
                                                    "menuName": "비빔밥",
                                                    "tags": ["매운맛", "한식"],
                                                    "thumbnailUrl": "https://asset.matchuri.com/menu-items/1001/sample.jpg"
                                                  }
                                                ],
                                                "pageInfo": {
                                                  "page": 0,
                                                  "size": 20,
                                                  "totalElements": 1,
                                                  "totalPages": 1,
                                                  "first": true,
                                                  "last": true,
                                                  "hasNext": false,
                                                  "hasPrevious": false
                                                }
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<PageResponse<PersonalRecommendationHistoryResponse>> getMyPersonalRecommendationList(
            @AuthenticatedMemberId Long memberId,
            @Parameter(description = "0부터 시작하는 페이지 번호입니다.", example = "0")
            @Min(0)
            Integer page,

            @Parameter(description = "페이지 크기입니다. 기본값은 20입니다.", example = "20")
            @Min(1)
            @Max(100)
            Integer size
    );
}
