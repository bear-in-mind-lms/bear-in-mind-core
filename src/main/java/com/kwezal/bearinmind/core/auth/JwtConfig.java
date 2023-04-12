package com.kwezal.bearinmind.core.auth;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class JwtConfig {

    @Value("${application.security.jwt.signing-key}")
    String signingKey;

    @Value("${application.security.jwt.cookie-name}")
    String cookieName;

    @Value("${application.security.jwt.authorization-header-prefix}")
    String authorizationHeaderPrefix;

    @Value("${application.security.jwt.lifetime-minutes}")
    Integer lifetimeMinutes;
}
