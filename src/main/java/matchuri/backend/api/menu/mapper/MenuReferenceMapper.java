package matchuri.backend.api.menu.mapper;

import java.util.List;
import matchuri.backend.api.menu.dto.request.CreateAdminAttributeCategoryRequest;
import matchuri.backend.api.menu.dto.request.CreateAdminIngredientRequest;
import matchuri.backend.api.menu.dto.request.UpdateAdminAttributeCategoryRequest;
import matchuri.backend.api.menu.dto.response.AdminAttributeCategoryResponse;
import matchuri.backend.api.menu.dto.response.AdminIngredientResponse;
import matchuri.backend.api.menu.dto.response.AttributeCategoryResponse;
import matchuri.backend.api.menu.dto.response.RestrictionIngredientResponse;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.CreateAdminIngredientCommand;
import matchuri.backend.domain.menu.command.UpdateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;
import matchuri.backend.domain.menu.result.AdminIngredientResult;
import matchuri.backend.domain.menu.result.AttributeCategoryResult;
import matchuri.backend.domain.menu.result.RestrictionIngredientResult;
import matchuri.backend.global.exception.RequestValidationException;
import org.springframework.stereotype.Component;

@Component
public class MenuReferenceMapper {

    public CreateAdminAttributeCategoryCommand toCreateAdminAttributeCategoryCommand(CreateAdminAttributeCategoryRequest request) {
        return new CreateAdminAttributeCategoryCommand(
                toCategoryType(request.categoryType()),
                request.code().trim(),
                request.name().trim(),
                request.sortOrder()
        );
    }

    public CreateAdminIngredientCommand toCreateAdminIngredientCommand(CreateAdminIngredientRequest request) {
        return new CreateAdminIngredientCommand(
                request.code().trim(),
                request.name().trim(),
                request.allergen(),
                request.sortOrder()
        );
    }

    public UpdateAdminAttributeCategoryCommand toUpdateAdminAttributeCategoryCommand(
            Long attributeCategoryId,
            UpdateAdminAttributeCategoryRequest request
    ) {
        return new UpdateAdminAttributeCategoryCommand(
                attributeCategoryId,
                trimNullable(request.name()),
                request.sortOrder(),
                request.isActive()
        );
    }

    public List<AdminAttributeCategoryResponse> toAdminAttributeCategoryResponses(List<AdminAttributeCategoryResult> results) {
        return results.stream()
                .map(this::toAdminAttributeCategoryResponse)
                .toList();
    }

    public List<AdminIngredientResponse> toAdminIngredientResponses(List<AdminIngredientResult> results) {
        return results.stream()
                .map(this::toAdminIngredientResponse)
                .toList();
    }

    public List<AttributeCategoryResponse> toAttributeCategoryResponses(List<AttributeCategoryResult> results) {
        return results.stream()
                .map(this::toAttributeCategoryResponse)
                .toList();
    }

    public List<RestrictionIngredientResponse> toRestrictionIngredientResponses(List<RestrictionIngredientResult> results) {
        return results.stream()
                .map(this::toRestrictionIngredientResponse)
                .toList();
    }

    private AttributeCategoryResponse toAttributeCategoryResponse(AttributeCategoryResult result) {
        return new AttributeCategoryResponse(
                result.id(),
                result.categoryType(),
                result.code(),
                result.name(),
                result.sortOrder()
        );
    }

    public AdminAttributeCategoryResponse toAdminAttributeCategoryResponse(AdminAttributeCategoryResult result) {
        return new AdminAttributeCategoryResponse(
                result.id(),
                result.categoryType(),
                result.code(),
                result.name(),
                result.sortOrder(),
                result.isActive()
        );
    }

    public AdminIngredientResponse toAdminIngredientResponse(AdminIngredientResult result) {
        return new AdminIngredientResponse(
                result.id(),
                result.code(),
                result.name(),
                result.allergen(),
                result.sortOrder(),
                result.isActive()
        );
    }

    private RestrictionIngredientResponse toRestrictionIngredientResponse(RestrictionIngredientResult result) {
        return new RestrictionIngredientResponse(
                result.id(),
                result.code(),
                result.name(),
                result.allergen(),
                result.sortOrder()
        );
    }

    private CategoryType toCategoryType(String rawCategoryType) {
        try {
            return CategoryType.valueOf(rawCategoryType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw RequestValidationException.invalidBodyField(
                    "categoryType",
                    "허용되지 않은 categoryType 입니다. 허용 값: FLAVOR, COOKING_METHOD, FOOD_CATEGORY, TEXTURE, TEMPERATURE"
            );
        }
    }

    private String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}
