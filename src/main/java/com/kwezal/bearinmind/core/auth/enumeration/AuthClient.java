package com.kwezal.bearinmind.core.auth.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kwezal.bearinmind.core.utils.EnumUtils;

public enum AuthClient {
    API,
    WEB;

    @JsonCreator
    public static AuthClient fromNameOrNull(final String name) {
        return EnumUtils.fromNameOrNull(AuthClient.class, name);
    }
}
