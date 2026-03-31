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
        descriptors[0] = fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부");
        descriptors[1] = fieldWithPath("data").type(JsonFieldType.OBJECT).description("성공 응답 payload");
        descriptors[2] = fieldWithPath("error").type(JsonFieldType.NULL).description("성공 시 null");
        System.arraycopy(dataFields, 0, descriptors, 3, dataFields.length);
        return responseFields(descriptors);
    }

    public static ResponseFieldsSnippet errorResponse() {
        return responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                fieldWithPath("data").type(JsonFieldType.NULL).description("실패 시 null"),
                fieldWithPath("error.status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("error.code").type(JsonFieldType.STRING).description("서비스 에러 코드"),
                fieldWithPath("error.message").type(JsonFieldType.STRING).description("사용자 노출 가능 메시지"),
                fieldWithPath("error.details").type(JsonFieldType.ARRAY).description("검증 실패 상세 목록. 없으면 빈 배열"),
                fieldWithPath("error.details[].source").type(JsonFieldType.STRING).optional().description("오류 입력 위치(BODY, QUERY, PATH)"),
                fieldWithPath("error.details[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                fieldWithPath("error.details[].reason").type(JsonFieldType.STRING).optional().description("검증 실패 사유")
        );
    }

    public static RequestHeadersSnippet bearerAuthorizationHeader() {
        return requestHeaders(
                headerWithName("Authorization").description("Bearer access token").optional()
        );
    }
}
