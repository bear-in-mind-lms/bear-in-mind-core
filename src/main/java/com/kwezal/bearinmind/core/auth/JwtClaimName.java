package com.kwezal.bearinmind.core.auth;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtClaimName {

    public static final String AUTHORITIES = "auth";
    public static final String LOCALE = "locl";
    public static final String USER_ID = "usid";
}
