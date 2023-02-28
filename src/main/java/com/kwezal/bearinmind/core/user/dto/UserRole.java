package com.kwezal.bearinmind.core.user.dto;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum UserRole {
    // USER ROLES
    ADMINISTRATOR,
    TEACHER,
    STUDENT,

    // USER ROLE GROUPS
    ADMINISTRATOR_ROLE_GROUP(ADMINISTRATOR, TEACHER, STUDENT),
    TEACHER_ROLE_GROUP(TEACHER, STUDENT),
    STUDENT_ROLE_GROUP(STUDENT);

    private static final String ROLE_PREFIX = "ROLE_";

    private final Set<UserRole> userRoles;

    UserRole(UserRole... userRoles) {
        this.userRoles = Arrays.stream(userRoles).collect(Collectors.toUnmodifiableSet());
    }

    public static UserRole valueOfAuthority(String authority) {
        if (authority.startsWith(ROLE_PREFIX)) {
            return valueOf(authority.substring(ROLE_PREFIX.length()));
        } else {
            throw new IllegalArgumentException("Role authority should start with '" + ROLE_PREFIX + "'");
        }
    }

    public Set<UserRole> getUserRoles() {
        return userRoles.isEmpty() ? Set.of(this) : userRoles;
    }

    public Set<String> getAuthorityNames() {
        return getUserRoles().stream().map(role -> ROLE_PREFIX + role.name()).collect(Collectors.toSet());
    }
}
