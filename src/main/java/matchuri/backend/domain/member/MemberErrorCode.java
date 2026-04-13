package matchuri.backend.domain.member;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다. memberId : {0}"),
    NOT_FOUND_LOGIN_ID(HttpStatus.NOT_FOUND, "해당 로그인 아이디의 회원을 찾을 수 없습니다. loginId : {0}"),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 로그인 아이디입니다. loginId : {0}"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다. nickname : {0}"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INACTIVE_MEMBER(HttpStatus.FORBIDDEN, "비활성화된 회원입니다. memberId : {0}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "MEMBER_";
    }
}
