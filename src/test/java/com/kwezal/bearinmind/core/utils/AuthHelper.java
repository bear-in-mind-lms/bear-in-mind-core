package com.kwezal.bearinmind.core.utils;

import com.kwezal.bearinmind.core.auth.JwtConfig;
import com.kwezal.bearinmind.core.auth.service.AuthJwtService;
import com.kwezal.bearinmind.core.user.dto.UserRole;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final EnumMap<UserRole, String> tokens = new EnumMap<>(UserRole.class);

    public static final long DEFAULT_USER_ID = 1;
    private static final String DEFAULT_USERNAME = "mrmasterkey";
    private static final String DEFAULT_LOCALE = "da";

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private AuthJwtService authJwtService;

    public WebTestClient.RequestHeadersSpec<?> asAdmin(WebTestClient.RequestHeadersSpec<?> spec) {
        return asUserWithRole(UserRole.ADMINISTRATOR_ROLE_GROUP, spec);
    }

    public WebTestClient.RequestHeadersSpec<?> asTeacher(WebTestClient.RequestHeadersSpec<?> spec) {
        return asUserWithRole(UserRole.TEACHER_ROLE_GROUP, spec);
    }

    public WebTestClient.RequestHeadersSpec<?> asStudent(WebTestClient.RequestHeadersSpec<?> spec) {
        return asUserWithRole(UserRole.STUDENT_ROLE_GROUP, spec);
    }

    public WebTestClient.RequestHeadersSpec<?> asUserWithRole(UserRole role, WebTestClient.RequestHeadersSpec<?> spec) {
        return asUserWithRoleAndId(role, DEFAULT_USER_ID, DEFAULT_USERNAME, DEFAULT_LOCALE, spec);
    }

    public WebTestClient.RequestHeadersSpec<?> asUserWithRoleAndId(
        UserRole role,
        long userId,
        String username,
        String locale,
        WebTestClient.RequestHeadersSpec<?> spec
    ) {
        return spec.header("Authorization", getToken(role, userId, username, locale));
    }

    private String getToken(UserRole role, long userId, String username, String locale) {
        return userId == DEFAULT_USER_ID
            ? tokens.computeIfAbsent(role, key -> createBearer(role, userId, username, locale))
            : createBearer(role, userId, username, locale);
    }

    private String createBearer(UserRole role, long userId, String username, String locale) {
        final var token = authJwtService.buildJwt(
            username,
            userId,
            locale,
            role.getAuthorityNames(),
            new Date(),
            Date.from(Instant.now().plus(Duration.ofDays(1)))
        );
        return jwtConfig.getAuthorizationHeaderPrefix() + token;
    }
}
