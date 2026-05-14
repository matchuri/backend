package matchuri.backend.domain.group.exception;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹을 찾을 수 없습니다. groupId : {0}"),
    NOT_ACTIVE(HttpStatus.CONFLICT, "활성 상태의 그룹이 아닙니다. groupId : {0}"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "그룹 접근 권한이 없습니다. groupId : {0}"),
    DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 삭제 권한이 없습니다. groupId : {0}"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹의 활성 멤버를 찾을 수 없습니다. groupId : {0}, memberId : {1}"),
    MEMBER_ALREADY_LEFT(HttpStatus.CONFLICT, "이미 나간 그룹 멤버입니다. groupId : {0}, memberId : {1}"),
    OWNER_LEAVE_NOT_ALLOWED(HttpStatus.CONFLICT, "그룹 방장은 나가기 대신 그룹 삭제를 사용해야 합니다. groupId : {0}"),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여 중인 그룹입니다. groupId : {0}, memberId : {1}"),
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹 초대 코드를 찾을 수 없습니다. inviteCode : {0}"),
    INVITE_EXPIRED(HttpStatus.CONFLICT, "만료된 그룹 초대 코드입니다. inviteCode : {0}"),
    INVITE_REVOKED(HttpStatus.CONFLICT, "취소된 그룹 초대 코드입니다. inviteCode : {0}"),
    INVITE_CODE_GENERATION_FAILED(HttpStatus.CONFLICT, "그룹 초대 코드 생성에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "GROUP_";
    }
}
