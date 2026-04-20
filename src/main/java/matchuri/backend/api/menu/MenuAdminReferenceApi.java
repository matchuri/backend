package matchuri.backend.api.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import matchuri.backend.api.menu.dto.docs.AdminAttributeCategoryApiResponse;
import matchuri.backend.api.menu.dto.docs.AdminAttributeCategoryListApiResponse;
import matchuri.backend.api.menu.dto.request.CreateAdminAttributeCategoryRequest;
import matchuri.backend.api.menu.dto.response.AdminAttributeCategoryResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Menu Admin Reference", description = "참조 데이터 운영 관리를 위한 관리자 전용 API")
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

    @Operation(
            summary = "관리자 attribute category 생성",
            description = """
                    운영 관리용 `attribute category`를 새로 생성합니다.

                    - `ADMIN` 권한이 필요합니다.
                    - 생성 직후 기본 활성 상태는 `true`입니다.
                    - 중복 기준은 `(categoryType, code)` 조합입니다.
                    - 성공 시 최신 단건 상태를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminAttributeCategoryApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": 10,
                                                "categoryType": "FLAVOR",
                                                "code": "SPICY",
                                                "name": "매운맛",
                                                "sortOrder": 10,
                                                "isActive": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 바디 필드가 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "invalidCategoryType",
                                    value = """
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
                                                    "field": "categoryType",
                                                    "reason": "허용되지 않은 categoryType 입니다. 허용 값: FLAVOR, COOKING_METHOD, FOOD_CATEGORY, TEXTURE, TEMPERATURE"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복된 attribute category",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "duplicate",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 409,
                                                "code": "MENU_ATTRIBUTE_CATEGORY_DUPLICATE",
                                                "message": "속성 카테고리가 이미 존재합니다. categoryType : FLAVOR, code : SPICY",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음"
            )
    })
    ApiResponse<AdminAttributeCategoryResponse> createAdminAttributeCategory(CreateAdminAttributeCategoryRequest request);
}
