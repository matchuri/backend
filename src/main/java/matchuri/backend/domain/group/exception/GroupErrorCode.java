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
    UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 수정 권한이 없습니다. groupId : {0}"),
    UPDATE_EMPTY_REQUEST(HttpStatus.BAD_REQUEST, "수정할 그룹 정보가 없습니다."),
    DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 삭제 권한이 없습니다. groupId : {0}"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹의 활성 멤버를 찾을 수 없습니다. groupId : {0}, memberId : {1}"),
    MEMBER_ALREADY_LEFT(HttpStatus.CONFLICT, "이미 나간 그룹 멤버입니다. groupId : {0}, memberId : {1}"),
    OWNER_LEAVE_NOT_ALLOWED(HttpStatus.CONFLICT, "그룹 방장은 나가기 대신 그룹 삭제를 사용해야 합니다. groupId : {0}"),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여 중인 그룹입니다. groupId : {0}, memberId : {1}"),
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹 초대 코드를 찾을 수 없습니다. inviteCode : {0}"),
    INVITE_EXPIRED(HttpStatus.CONFLICT, "만료된 그룹 초대입니다. inviteIdOrCode : {0}"),
    INVITE_REVOKED(HttpStatus.CONFLICT, "취소된 그룹 초대입니다. inviteIdOrCode : {0}"),
    INVITE_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 초대 권한이 없습니다. groupId : {0}"),
    INVITE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹 초대 요청을 찾을 수 없습니다. inviteId : {0}"),
    INVITE_RESPONSE_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 초대 응답 권한이 없습니다. inviteId : {0}"),
    INVITE_NOT_PENDING(HttpStatus.CONFLICT, "응답 가능한 대기 상태의 그룹 초대가 아닙니다. inviteId : {0}, status : {1}"),
    INVITE_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "초대 대상 회원을 찾을 수 없습니다. nickname : {0}"),
    INVITE_SELF_NOT_ALLOWED(HttpStatus.CONFLICT, "자기 자신은 그룹에 초대할 수 없습니다. memberId : {0}"),
    INVITE_TARGET_ALREADY_MEMBER(HttpStatus.CONFLICT, "이미 그룹에 참여 중인 회원입니다. groupId : {0}, memberId : {1}"),
    INVITE_ALREADY_PENDING(HttpStatus.CONFLICT, "이미 대기 중인 그룹 초대가 있습니다. groupId : {0}, memberId : {1}"),
    INVITE_CODE_GENERATION_FAILED(HttpStatus.CONFLICT, "그룹 고정 초대 코드 생성에 실패했습니다."),
    RECOMMENDATION_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 추천 생성 권한이 없습니다. groupId : {0}"),
    RECOMMENDATION_OPEN_EXISTS(HttpStatus.CONFLICT, "이미 열린 그룹 추천이 있습니다. groupId : {0}"),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 그룹 추천을 찾을 수 없습니다. sessionId : {0}"),
    RECOMMENDATION_NOT_OPEN(HttpStatus.CONFLICT, "열린 상태의 그룹 추천이 아닙니다. sessionId : {0}"),
    RECOMMENDATION_REROLL_FORBIDDEN(HttpStatus.FORBIDDEN, "그룹 추천 재요청 권한이 없습니다. groupId : {0}");

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
