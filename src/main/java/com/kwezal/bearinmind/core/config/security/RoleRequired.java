package com.kwezal.bearinmind.core.config.security;

import com.kwezal.bearinmind.core.user.dto.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleRequired {
    UserRole[] value();
}
