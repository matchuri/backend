package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoUnlinkClient implements UnlinkClient {

    public static final String URL = "https://kapi.kakao.com/v1/user/unlink";
    private final MemberRepository memberRepository;

    @Value("${kakao.admin-key}")
    private String adminKey;

    @Override
    public boolean support(SocialProviderType socialProviderType) {
        return socialProviderType == SocialProviderType.KAKAO;
    }

    @Override
    public void unlink(Long memberId) {
        String providerId = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND)).getSocialProviderUserId();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", providerId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        UnlinkResult result = restTemplate.exchange(URL, HttpMethod.POST, request, UnlinkResult.class).getBody();

        if (result != null) log.info("KakaoUnlinkClient unlink result: {}", result);
    }

    public record UnlinkResult(
            Long id
    ) {
    }
}
