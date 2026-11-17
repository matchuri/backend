package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.global.exception.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2Unlinker {

    private final MemberReader memberReader;
    private final OAuth2UnlinkClientFactory oAuth2UnlinkClientFactory;

    public void unlink(Long memberId) {
        Member member = memberReader.get(memberId);
        if (!member.isSocial()) {
            return;
        }
        UnlinkClient client = oAuth2UnlinkClientFactory.getClient(member.getSocialProviderType())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED));
        client.unlink(member.getId());
    }
}
