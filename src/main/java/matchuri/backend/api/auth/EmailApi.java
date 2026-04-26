package matchuri.backend.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import matchuri.backend.api.auth.dto.request.SendEmailRequest;
import matchuri.backend.api.auth.dto.response.SendEmailResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Email", description = "이메일 관련 API")
public interface EmailApi {

    @Operation(
            summary = "이메일 인증",
            description = """
                    명세 추후 작성
                    """
    )
    ApiResponse<SendEmailResponse> sendTxtEmail(SendEmailRequest request);
}
