package matchuri.backend.domain.menu;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MenuErrorCode implements ErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다. menuId : {0}"),
    ATTRIBUTE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "속성 카테고리를 찾을 수 없습니다. attributeCategoryId : {0}"),
    ATTRIBUTE_CATEGORY_DUPLICATE(HttpStatus.CONFLICT, "속성 카테고리가 이미 존재합니다. categoryType : {0}, code : {1}"),
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "재료를 찾을 수 없습니다. ingredientId : {0}"),
    INGREDIENT_DUPLICATE(HttpStatus.CONFLICT, "재료가 이미 존재합니다. code : {0}"),
    INVALID_FILTER(HttpStatus.BAD_REQUEST, "메뉴 검색 필터가 유효하지 않습니다. {0} : {1}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "MENU_";
    }
}
