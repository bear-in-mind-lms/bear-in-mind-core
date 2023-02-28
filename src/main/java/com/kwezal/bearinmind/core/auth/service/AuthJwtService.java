package com.kwezal.bearinmind.core.auth.service;

import com.kwezal.bearinmind.core.auth.JwtClaimName;
import com.kwezal.bearinmind.core.auth.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuthJwtService {

    private final JwtConfig jwtConfig;
    private final Key signingKey;

    public AuthJwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSigningKey().getBytes());
    }

    public String buildJwt(
        final String username,
        final Long userId,
        final String locale,
        final Set<String> authorities,
        final Date issuedAt,
        final Date expiration
    ) {
        return Jwts
            .builder()
            .setSubject(username)
            .claim(JwtClaimName.AUTHORITIES, authorities)
            .claim(JwtClaimName.LOCALE, locale)
            .claim(JwtClaimName.USER_ID, userId)
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .signWith(signingKey)
            .compact();
    }

    public String buildJwt(final String username, final Long userId, final String locale, final Set<String> authorities) {
        return buildJwt(
            username,
            userId,
            locale,
            authorities,
            new Date(),
            Date.from(Instant.now().plus(Duration.ofMinutes(jwtConfig.getLifetimeMinutes())))
        );
    }

    public Claims parseClaims(final String token) {
        return parseJws(token).getBody();
    }

    private Jws<Claims> parseJws(final String token) {
        final var jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();

        return jwtParser.parseClaimsJws(token);
    }
}
