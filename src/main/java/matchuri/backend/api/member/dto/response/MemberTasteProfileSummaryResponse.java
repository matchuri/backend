package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record MemberTasteProfileSummaryResponse(
        @Schema(description = "현재 로그인한 회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "현재 프로필 정책/구조가 어떤 버전을 따르는지 나타내는 서버 관리 버전입니다.", example = "v1")
        String profileVersion,

        @Schema(description = "현재 선택된 attribute category 목록입니다.")
        List<MemberTasteAttributeCategoryResponse> attributeCategories,

        @Schema(description = "현재 선택된 restriction ingredient 목록입니다.")
        List<MemberTasteRestrictionIngredientResponse> restrictionIngredients,

        @Schema(description = "취향 프로필이 마지막으로 갱신된 시각입니다. 프로필이 없으면 null입니다.", example = "2026-04-17T12:30:45")
        LocalDateTime updatedAt
) {
}
