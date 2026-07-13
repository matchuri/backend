package matchuri.backend.domain.member.exception;

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
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다. email : {0}"),
    DUPLICATE_TASTE_ATTRIBUTE_CATEGORY(HttpStatus.BAD_REQUEST,
            "중복된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : {0}"),
    DUPLICATE_TASTE_RESTRICTION_INGREDIENT(HttpStatus.BAD_REQUEST,
            "중복된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : {0}"),
    DUPLICATE_TASTE_DISLIKED_MENU_ITEM(HttpStatus.BAD_REQUEST,
            "중복된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : {0}"),
    INVALID_TASTE_ATTRIBUTE_CATEGORY(HttpStatus.BAD_REQUEST,
            "유효하지 않거나 비활성화된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : {0}"),
    INVALID_TASTE_RESTRICTION_INGREDIENT(HttpStatus.BAD_REQUEST,
            "유효하지 않거나 비활성화된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : {0}"),
    INVALID_TASTE_DISLIKED_MENU_ITEM(HttpStatus.BAD_REQUEST,
            "유효하지 않거나 비활성화된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : {0}"),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "저장된 개인 위치를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    NICKNAME_REQUIRED(HttpStatus.FORBIDDEN, "닉네임 설정이 필요합니다."),
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
