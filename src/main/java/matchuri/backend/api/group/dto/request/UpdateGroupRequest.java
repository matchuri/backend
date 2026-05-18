package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(
        @Schema(description = "변경할 그룹 방 이름입니다. 생략하면 변경하지 않습니다.", example = "점심 회의방")
        @Size(max = 100, message = "name은 100자를 초과할 수 없습니다.")
        String name
) {

    @AssertTrue(message = "name은 비어 있을 수 없습니다.")
    public boolean isNameNullOrNotBlank() {
        return name == null || !name.isBlank();
    }
}
