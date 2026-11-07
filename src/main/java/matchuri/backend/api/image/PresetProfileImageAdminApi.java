package matchuri.backend.api.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import matchuri.backend.api.image.dto.docs.PresetProfileImageApiResponse;
import matchuri.backend.api.image.dto.docs.PresetProfileImageListApiResponse;
import matchuri.backend.api.image.dto.response.PresetProfileImageResponse;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Preset Profile Image", description = "관리자 프리셋 프로필 이미지 운영 API")
public interface PresetProfileImageAdminApi {

    @Operation(summary = "프리셋 프로필 이미지 목록", description = "삭제되지 않은 프리셋을 ID 순서로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PresetProfileImageListApiResponse.class)
                    )
            )
    })
    ApiResponse<List<PresetProfileImageResponse>> getPresetProfileImages();

    @Operation(
            summary = "프리셋 프로필 이미지 추가",
            description = "JPEG, PNG, WebP 파일을 R2에 업로드하고 비기본 프리셋으로 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프리셋 추가 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PresetProfileImageApiResponse.class)
                    )
            )
    })
    ApiResponse<PresetProfileImageResponse> upload(MultipartFile file);

    @Operation(
            summary = "프리셋 프로필 이미지 삭제",
            description = "프리셋을 soft delete합니다. 현재 기본 프리셋은 다른 기본을 먼저 지정해야 삭제할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "기본 프리셋 삭제 시도",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 409,
                                                "code": "IMAGE_DEFAULT_PRESET_PROFILE_DELETE_NOT_ALLOWED",
                                                "message": "기본 프리셋 프로필 이미지는 삭제할 수 없습니다. 다른 이미지를 기본으로 먼저 설정해 주세요.",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> delete(Long presetProfileImageId);

    @Operation(
            summary = "기본 프리셋 프로필 이미지 설정",
            description = "기존 기본 상태를 모두 해제하고 선택한 활성 프리셋 하나만 기본으로 설정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "기본 프리셋 설정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PresetProfileImageApiResponse.class)
                    )
            )
    })
    ApiResponse<PresetProfileImageResponse> setDefault(Long presetProfileImageId);
}
