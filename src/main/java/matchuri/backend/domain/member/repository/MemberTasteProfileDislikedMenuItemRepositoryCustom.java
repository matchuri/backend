package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;

public interface MemberTasteProfileDislikedMenuItemRepositoryCustom {

    List<MemberTasteProfileDislikedMenuItem> findAllByProfileIdOrderByDisplay(Long profileId);
}
