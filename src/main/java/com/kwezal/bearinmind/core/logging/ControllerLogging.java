package com.kwezal.bearinmind.core.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerLogging {
    /**
     * A basic path of a controller.
     */
    String value();
}
