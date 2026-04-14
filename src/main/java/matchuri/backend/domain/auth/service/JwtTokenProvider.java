package matchuri.backend.domain.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.service.RequiredAgreementRevisionResolver;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final MatchuriProperties matchuriProperties;
    private final RequiredAgreementRevisionResolver requiredAgreementRevisionResolver;
    private final Clock clock = Clock.systemUTC();

    public TokenPair issueTokenPair(Member member) {
        return issueTokenPair(member, requiredAgreementRevisionResolver.resolve(member.getId()));
    }

    public TokenPair issueTokenPair(Member member, String requiredAgreementRevision) {
        MatchuriProperties.Jwt jwt = matchuriProperties.getAuth().getJwt();
        IssuedAccessToken issuedAccessToken = issueAccessToken(member, requiredAgreementRevision);
        Instant now = clock.instant();
        Instant refreshTokenExpiresAt = now.plusSeconds(jwt.getRefreshTokenExpirationSeconds());

        String refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();

        return new TokenPair(
                issuedAccessToken.accessToken(),
                issuedAccessToken.expiresIn(),
                refreshToken,
                LocalDateTime.ofInstant(refreshTokenExpiresAt, ZoneOffset.UTC)
        );
    }

    public IssuedAccessToken issueAccessToken(Member member) {
        return issueAccessToken(member, requiredAgreementRevisionResolver.resolve(member.getId()));
    }

    public IssuedAccessToken issueAccessToken(Member member, String requiredAgreementRevision) {
        Instant now = clock.instant();
        MatchuriProperties.Jwt jwt = matchuriProperties.getAuth().getJwt();
        Instant accessTokenExpiresAt = now.plusSeconds(jwt.getAccessTokenExpirationSeconds());

        String accessToken = Jwts.builder()
                .issuer(jwt.getIssuer())
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getMemberRole().name())
                .claim("loginId", member.getLoginId())
                .claim("requiredAgreementRevision", requiredAgreementRevision)
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessTokenExpiresAt))
                .signWith(signingKey())
                .compact();

        return new IssuedAccessToken(accessToken, jwt.getAccessTokenExpirationSeconds());
    }

    public JwtClaims parseAccessToken(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtClaims(
                Long.parseLong(claims.getSubject()),
                claims.get("role", String.class),
                claims.get("loginId", String.class),
                claims.get("requiredAgreementRevision", String.class)
        );
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(matchuriProperties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public record JwtClaims(
            Long memberId,
            String role,
            String loginId,
            String requiredAgreementRevision
    ) {
    }
}
