package com.kwezal.bearinmind.core.auth;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class JwtConfig {

    @Value("${application.security.jwt.signing-key}")
    private String signingKey;

    @Value("${application.security.jwt.cookie-name}")
    private String cookieName;

    @Value("${application.security.jwt.authorization-header-prefix}")
    private String authorizationHeaderPrefix;

    @Value("${application.security.jwt.lifetime-minutes}")
    private Integer lifetimeMinutes;
}
