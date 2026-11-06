package matchuri.backend.domain.member.support.profile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.image.support.ImageUrlResolver;
import matchuri.backend.domain.member.entity.MemberProfileImage;
import matchuri.backend.domain.member.repository.MemberProfileImageRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberProfileImageUrlResolver {

    private final MemberProfileImageRepository memberProfileImageRepository;
    private final ImageUrlResolver imageUrlResolver;

    public Map<Long, String> resolveAll(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctMemberIds = memberIds.stream()
                .distinct()
                .toList();

        return memberProfileImageRepository.findAllByMemberIdIn(distinctMemberIds).stream()
                .collect(Collectors.toMap(
                        profileImage -> profileImage.getMember().getId(),
                        this::toPublicUrl
                ));
    }

    private String toPublicUrl(MemberProfileImage profileImage) {
        return imageUrlResolver.toPublicUrl(profileImage.getImageAsset().getObjectKey());
    }
}
