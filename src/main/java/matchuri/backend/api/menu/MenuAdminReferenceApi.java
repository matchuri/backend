package matchuri.backend.api.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import matchuri.backend.api.menu.dto.docs.AdminAttributeCategoryListApiResponse;
import matchuri.backend.api.menu.dto.response.AdminAttributeCategoryResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Menu Admin Reference", description = "참조 데이터 운영 관리를 위한 관리자 전용 조회 API")
@SecurityRequirement(name = "bearerAuth")
public interface MenuAdminReferenceApi {

    @Operation(
            summary = "관리자 attribute category 목록 조회",
            description = """
                    운영 관리용 `attribute category` 목록을 조회합니다.

                    - `ADMIN` 권한이 필요합니다.
                    - 활성/비활성 데이터를 모두 반환합니다.
                    - 별도 `includeInactive` 파라미터 없이 전체 운영 상태를 기본 노출합니다.
                    - 정렬 기준은 `categoryType`, `sortOrder`, `id`입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminAttributeCategoryListApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "categoryType": "FLAVOR",
                                                  "code": "SPICY",
                                                  "name": "매운맛",
                                                  "sortOrder": 10,
                                                  "isActive": true
                                                },
                                                {
                                                  "id": 2,
                                                  "categoryType": "FLAVOR",
                                                  "code": "MILD",
                                                  "name": "순한맛",
                                                  "sortOrder": 20,
                                                  "isActive": false
                                                }
                                              ],
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "forbidden",
                                    value = """
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
                                            """
                            )
                    )
            )
    })
    ApiResponse<List<AdminAttributeCategoryResponse>> getAdminAttributeCategories();
}
