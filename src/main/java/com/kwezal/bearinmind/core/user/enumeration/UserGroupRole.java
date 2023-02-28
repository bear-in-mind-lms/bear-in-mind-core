package com.kwezal.bearinmind.core.user.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kwezal.bearinmind.core.utils.EnumUtils;

public enum UserGroupRole {
    OWNER,
    MEMBER,
    INVITED,
    APPLICANT;

    @JsonCreator
    public static UserGroupRole fromNameOrNull(final String name) {
        return EnumUtils.fromNameOrNull(UserGroupRole.class, name);
    }
}
