package matchuri.backend.global.docs;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public final class RestDocsSupport {

    private RestDocsSupport() {
    }

    public static ResponseFieldsSnippet successResponse(FieldDescriptor... dataFields) {
        FieldDescriptor[] descriptors = new FieldDescriptor[3 + dataFields.length];
        descriptors[0] = fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                .description("요청 성공 여부. 정상 처리되면 true입니다.");
        descriptors[1] = fieldWithPath("data").type(JsonFieldType.OBJECT)
                .description("성공 응답 payload. 엔드포인트별 상세 필드는 data 하위에 위치합니다.");
        descriptors[2] = fieldWithPath("error").type(JsonFieldType.NULL)
                .description("성공 시 null입니다.");
        System.arraycopy(dataFields, 0, descriptors, 3, dataFields.length);
        return responseFields(descriptors);
    }

    public static ResponseFieldsSnippet errorResponse() {
        return responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                        .description("요청 성공 여부. 실패 응답에서는 항상 false입니다."),
                fieldWithPath("data").type(JsonFieldType.NULL)
                        .description("실패 시 null입니다."),
                fieldWithPath("error.status").type(JsonFieldType.NUMBER)
                        .description("HTTP 상태 코드입니다. 예: 400, 401, 404, 409, 500"),
                fieldWithPath("error.code").type(JsonFieldType.STRING)
                        .description("서비스 에러 코드입니다. 예: COMMON_INVALID_PATH_VARIABLE"),
                fieldWithPath("error.message").type(JsonFieldType.STRING)
                        .description("사용자에게 노출 가능한 요약 메시지입니다."),
                fieldWithPath("error.details").type(JsonFieldType.ARRAY)
                        .description("검증 실패 상세 목록입니다. 검증 예외가 아니면 빈 배열일 수 있습니다."),
                fieldWithPath("error.details[].source").type(JsonFieldType.STRING).optional()
                        .description("오류 입력 위치입니다. BODY, QUERY, PATH 중 하나입니다."),
                fieldWithPath("error.details[].field").type(JsonFieldType.STRING).optional()
                        .description("검증에 실패한 필드명 또는 파라미터명입니다."),
                fieldWithPath("error.details[].reason").type(JsonFieldType.STRING).optional()
                        .description("제약 위반 사유입니다. 길이 제한, 허용 문자, 공백 금지 등의 구체 메시지가 들어갑니다.")
        );
    }

    public static RequestHeadersSnippet bearerAuthorizationHeader() {
        return requestHeaders(
                headerWithName("Authorization")
                        .description("인증이 필요한 API에서 사용하는 Bearer access token 헤더입니다. 형식: Bearer {token}")
                        .optional()
        );
    }
}
