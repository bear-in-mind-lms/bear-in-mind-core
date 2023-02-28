package com.kwezal.bearinmind.core.validation.annotation;

import com.kwezal.bearinmind.core.validation.LocaleValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = LocaleValidator.class)
@Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Locale {
    String message() default "must contain two lowercase letters, optionally followed by two uppercase letters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean allowEmpty() default false;
}
