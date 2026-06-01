package matchuri.backend.domain.image.exception;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    UPLOAD_FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드 파일이 비어 있습니다."),
    UPLOAD_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "업로드 파일 크기가 너무 큽니다. maxBytes : {0}"),
    UNSUPPORTED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다. contentType : {0}"),
    INVALID_CONTENT(HttpStatus.BAD_REQUEST, "올바른 이미지 파일이 아닙니다."),
    INVALID_RESOLUTION(HttpStatus.BAD_REQUEST, "이미지 해상도가 허용 범위를 벗어났습니다. width : {0}, height : {1}"),
    STORAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장소 업로드에 실패했습니다."),
    STORAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장소 삭제에 실패했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다. imageId : {0}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "IMAGE_";
    }
}
