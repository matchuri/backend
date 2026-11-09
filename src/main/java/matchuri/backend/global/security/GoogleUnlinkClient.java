package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.support.token.ProviderTokenService;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleUnlinkClient implements UnlinkClient {
    private final static String URL = "https://oauth2.googleapis.com/revoke";

    private final ProviderTokenService providerTokenService;

    @Override
    public boolean support(SocialProviderType socialProviderType) {
        return socialProviderType == SocialProviderType.GOOGLE;
    }

    @Override
    public void unlink(Long memberId) {
        String providerRefreshToken = providerTokenService.findByMemberId(memberId);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token",  providerRefreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.exchange(URL, HttpMethod.POST, request, Void.class);
    }
}
