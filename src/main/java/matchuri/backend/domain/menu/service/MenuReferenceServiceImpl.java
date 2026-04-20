package matchuri.backend.domain.menu.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.result.AttributeCategoryResult;
import matchuri.backend.domain.menu.result.RestrictionIngredientResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuReferenceServiceImpl implements MenuReferenceService {

    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    public List<AttributeCategoryResult> getActiveAttributeCategories() {
        return attributeCategoryRepository.findAllByActiveTrueOrderByCategoryTypeAscSortOrderAscIdAsc().stream()
                .map(AttributeCategoryResult::from)
                .toList();
    }

    @Override
    public List<RestrictionIngredientResult> getActiveRestrictionIngredients() {
        return ingredientRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(RestrictionIngredientResult::from)
                .toList();
    }
}
