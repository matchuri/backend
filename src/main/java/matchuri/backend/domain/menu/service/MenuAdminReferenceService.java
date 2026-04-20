package matchuri.backend.domain.menu.service;

import java.util.List;
import matchuri.backend.domain.menu.command.CreateAdminAttributeCategoryCommand;
import matchuri.backend.domain.menu.result.AdminAttributeCategoryResult;

public interface MenuAdminReferenceService {

    List<AdminAttributeCategoryResult> getAttributeCategories();

    AdminAttributeCategoryResult createAttributeCategory(CreateAdminAttributeCategoryCommand command);
}
