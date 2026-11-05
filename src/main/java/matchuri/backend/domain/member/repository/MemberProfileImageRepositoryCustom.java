package matchuri.backend.domain.member.repository;

import java.util.List;
import matchuri.backend.domain.member.entity.MemberProfileImage;

public interface MemberProfileImageRepositoryCustom {

    List<MemberProfileImage> findAllByMemberIdIn(List<Long> memberIds);

    int updateToDefault(Long deletedAssetId, Long updateAssetId);
}
