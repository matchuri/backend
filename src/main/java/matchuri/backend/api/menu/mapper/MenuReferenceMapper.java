package matchuri.backend.api.menu.mapper;

import java.util.List;
import matchuri.backend.api.menu.dto.response.AttributeCategoryResponse;
import matchuri.backend.api.menu.dto.response.RestrictionIngredientResponse;
import matchuri.backend.domain.menu.result.AttributeCategoryResult;
import matchuri.backend.domain.menu.result.RestrictionIngredientResult;
import org.springframework.stereotype.Component;

@Component
public class MenuReferenceMapper {

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

    private RestrictionIngredientResponse toRestrictionIngredientResponse(RestrictionIngredientResult result) {
        return new RestrictionIngredientResponse(
                result.id(),
                result.code(),
                result.name(),
                result.allergen(),
                result.sortOrder()
        );
    }
}
