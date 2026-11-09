package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.support.token.ProviderTokenService;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class NaverUnlinkClient implements UnlinkClient {
    private static final String URL = "https://nid.naver.com/oauth2.0/revoke";

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    private final ProviderTokenService providerTokenService;
    private final RestTemplate restTemplate;

    @Override
    public boolean support(SocialProviderType socialProviderType) {
        return socialProviderType == SocialProviderType.NAVER;
    }

    @Override
    public void unlink(Long memberId) {
        String providerRefreshToken = providerTokenService.findByMemberId(memberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("token", providerRefreshToken);
        body.add("token_type_hint", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.exchange(URL, HttpMethod.POST, request, Void.class);
    }
}
