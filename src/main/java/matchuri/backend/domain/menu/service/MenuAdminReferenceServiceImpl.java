package matchuri.backend.domain.menu.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;
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
}
