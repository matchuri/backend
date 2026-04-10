package matchuri.backend.domain.member;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberAgreementErrorCode implements ErrorCode {

    REQUIRED(HttpStatus.FORBIDDEN, "필수 약관 동의가 필요합니다."),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 약관 종류입니다. agreementType : {0}"),
    REQUIRED_TYPES_MISSING(HttpStatus.BAD_REQUEST, "필수 약관 동의 요청이 누락되었습니다. missingTypes : {0}"),
    VERSION_MISMATCH(HttpStatus.CONFLICT, "최신 필수 약관 버전과 일치하지 않습니다. agreementType : {0}, requestedVersion : {1}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "MEMBER_AGREEMENT_";
    }
}
