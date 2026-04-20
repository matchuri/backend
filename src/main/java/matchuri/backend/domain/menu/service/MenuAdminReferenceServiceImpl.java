package matchuri.backend.domain.menu.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.MenuErrorCode;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuAdminReferenceServiceImpl implements MenuAdminReferenceService {

    private final AttributeCategoryRepository attributeCategoryRepository;

    @Override
    public List<AdminAttributeCategoryResult> getAttributeCategories() {
        return attributeCategoryRepository.findAllByOrderByCategoryTypeAscSortOrderAscIdAsc().stream()
                .map(AdminAttributeCategoryResult::from)
                .toList();
    }

    @Override
    @Transactional
    public AdminAttributeCategoryResult createAttributeCategory(CreateAdminAttributeCategoryCommand command) {
        if (attributeCategoryRepository.existsByCategoryTypeAndCode(command.categoryType(), command.code())) {
            throw new BusinessException(MenuErrorCode.ATTRIBUTE_CATEGORY_DUPLICATE, command.categoryType(), command.code());
        }

        AttributeCategory attributeCategory = attributeCategoryRepository.saveAndFlush(
                new AttributeCategory(command.categoryType(), command.code(), command.name(), command.sortOrder())
        );

        return AdminAttributeCategoryResult.from(attributeCategory);
    }
}
