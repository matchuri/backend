package matchuri.backend.api.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import matchuri.backend.api.menu.dto.docs.AttributeCategoryListApiResponse;
import matchuri.backend.api.menu.dto.docs.MenuItemSummaryListApiResponse;
import matchuri.backend.api.menu.dto.docs.RestrictionIngredientListApiResponse;
import matchuri.backend.api.menu.dto.response.AttributeCategoryResponse;
import matchuri.backend.api.menu.dto.response.MenuItemSummaryResponse;
import matchuri.backend.api.menu.dto.response.RestrictionIngredientResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Menu Reference", description = "취향 입력과 메뉴 분류에 공통으로 쓰는 참조 데이터 조회 API")
public interface MenuReferenceApi {

    @Operation(
            summary = "attribute category 목록 조회",
            description = """
                    취향 입력과 메뉴 분류에 공통으로 사용하는 활성 `attribute category` 목록을 조회합니다.
                    
                    - 인증 없이 호출할 수 있습니다.
                    - 응답에는 `is_active=true`인 데이터만 포함됩니다.
                    - 정렬 기준은 `categoryType`, `sortOrder`, `id`입니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AttributeCategoryListApiResponse.class),
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
                                                  "sortOrder": 10
                                                }
                                              ],
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<List<AttributeCategoryResponse>> getAttributeCategories();

    @Operation(
            summary = "restriction ingredient 목록 조회",
            description = """
                    취향 입력에서 제한 재료로 선택할 수 있는 활성 `restriction ingredient` 목록을 조회합니다.
                    
                    - 인증 없이 호출할 수 있습니다.
                    - 응답에는 `is_active=true`인 데이터만 포함됩니다.
                    - 정렬 기준은 `sortOrder`, `id`입니다.
                    - 응답에는 UI 표시를 위한 `allergen` 여부를 함께 포함합니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestrictionIngredientListApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {
                                                  "id": 101,
                                                  "code": "PEANUT",
                                                  "name": "땅콩",
                                                  "allergen": true,
                                                  "sortOrder": 10
                                                }
                                              ],
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<List<RestrictionIngredientResponse>> getRestrictionIngredients();

    @Operation(
            summary = "메뉴 목록 조회",
            description = """
                    활성 메뉴 목록을 조회합니다.
                    
                    - 인증 없이 호출할 수 있습니다.
                    - 응답에는 `is_active=true`인 메뉴만 포함됩니다.
                    - 목록 응답은 `id`, `code`, `name`만 포함합니다.
                    - `query`는 메뉴명 부분 검색입니다.
                    - `attributeCategoryIds`, `ingredientIds`는 각 그룹 내부 OR 조건으로 처리합니다.
                    - `query`, `attributeCategoryIds`, `ingredientIds` 그룹 간 조합은 AND 조건으로 처리합니다.
                    - 존재하지 않거나 비활성화된 필터 ID가 포함되면 4xx로 거절합니다.
                    - 정렬 기준은 `id` 오름차순입니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MenuItemSummaryListApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {
                                                  "id": 1001,
                                                  "code": "PORK_CUTLET",
                                                  "name": "돈까스"
                                                }
                                              ],
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<List<MenuItemSummaryResponse>> searchMenuItems(
            String query,
            List<Long> attributeCategoryIds,
            List<Long> ingredientIds
    );
}
