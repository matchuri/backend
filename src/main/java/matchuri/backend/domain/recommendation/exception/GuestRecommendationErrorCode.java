package matchuri.backend.domain.recommendation.exception;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GuestRecommendationErrorCode implements ErrorCode {

    DUPLICATE_ATTRIBUTE_CATEGORY(HttpStatus.BAD_REQUEST,
            "중복된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : {0}"),
    DUPLICATE_RESTRICTION_INGREDIENT(HttpStatus.BAD_REQUEST,
            "중복된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : {0}"),
    DUPLICATE_DISLIKED_MENU_ITEM(HttpStatus.BAD_REQUEST,
            "중복된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : {0}"),
    INVALID_ATTRIBUTE_CATEGORY(HttpStatus.BAD_REQUEST,
            "유효하지 않거나 비활성화된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : {0}"),
    INVALID_RESTRICTION_INGREDIENT(HttpStatus.BAD_REQUEST,
            "유효하지 않거나 비활성화된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : {0}"),
    INVALID_DISLIKED_MENU_ITEM(HttpStatus.BAD_REQUEST,
            "유효하지 않거나 비활성화된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : {0}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "GUEST_RECOMMENDATION_";
    }
}
