package com.kwezal.bearinmind.core.config.security;

import static com.kwezal.bearinmind.core.utils.LoggingUtils.JWT_PARSING_EXCEPTION_MESSAGE;
import static java.util.Objects.nonNull;

import com.kwezal.bearinmind.core.auth.JwtClaimName;
import com.kwezal.bearinmind.core.auth.JwtConfig;
import com.kwezal.bearinmind.core.auth.service.AuthJwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final AuthJwtService authJwtService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final var token = readTokenFromRequest(request);
        if (nonNull(token)) {
            try {
                final var claims = authJwtService.parseClaims(token);
                final var username = claims.getSubject();
                final var claimsAuthorities = (List<String>) claims.get(JwtClaimName.AUTHORITIES);
                final var authorities = claimsAuthorities.stream().map(SimpleGrantedAuthority::new).toList();
                final var locale = (String) claims.get(JwtClaimName.LOCALE);
                final var userId = ((Number) claims.get(JwtClaimName.USER_ID)).longValue();

                final var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new JwtAuthenticationDetails(locale, userId));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.error(JWT_PARSING_EXCEPTION_MESSAGE, token);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String readTokenFromRequest(HttpServletRequest request) {
        final var authorizationHeader = request.getHeader("Authorization");
        if (nonNull(authorizationHeader) && authorizationHeader.startsWith(jwtConfig.getAuthorizationHeaderPrefix())) {
            return authorizationHeader.substring(jwtConfig.getAuthorizationHeaderPrefix().length());
        }

        final var cookies = request.getCookies();
        if (nonNull(cookies)) {
            final var tokenCookie = Arrays
                .stream(cookies)
                .filter(cookie -> jwtConfig.getCookieName().equals(cookie.getName()))
                .findAny();
            return tokenCookie.map(Cookie::getValue).orElse(null);
        }

        return null;
    }
}
