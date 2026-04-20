package matchuri.backend.domain.menu.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.MenuErrorCode;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.command.UpdateAdminAttributeCategoryCommand;
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

    @Override
    @Transactional
    public AdminAttributeCategoryResult updateAttributeCategory(UpdateAdminAttributeCategoryCommand command) {
        AttributeCategory attributeCategory = attributeCategoryRepository.findById(command.attributeCategoryId())
                .orElseThrow(() -> new BusinessException(
                        MenuErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND,
                        command.attributeCategoryId()
                ));

        if (command.name() != null) {
            attributeCategory.updateName(command.name());
        }

        if (command.sortOrder() != null) {
            attributeCategory.updateSortOrder(command.sortOrder());
        }

        if (command.isActive() != null) {
            if (command.isActive()) {
                attributeCategory.activate();
            } else {
                attributeCategory.deactivate();
            }
        }

        return AdminAttributeCategoryResult.from(attributeCategory);
    }

    @Override
    @Transactional
    public AdminAttributeCategoryResult deactivateAttributeCategory(Long attributeCategoryId) {
        AttributeCategory attributeCategory = attributeCategoryRepository.findById(attributeCategoryId)
                .orElseThrow(() -> new BusinessException(
                        MenuErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND,
                        attributeCategoryId
                ));

        if (attributeCategory.isActive()) {
            attributeCategory.deactivate();
        }

        return AdminAttributeCategoryResult.from(attributeCategory);
    }
}
