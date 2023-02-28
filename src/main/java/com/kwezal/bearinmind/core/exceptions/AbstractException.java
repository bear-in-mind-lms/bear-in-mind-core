package com.kwezal.bearinmind.core.exceptions;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
public abstract class AbstractException extends RuntimeException {

    private final String className;
    private final Map<String, Object> properties;

    private final String errorCode;

    @Nullable
    private final List<String> errorArguments;

    protected AbstractException(
        final Class<?> objectClass,
        final Map<String, Object> properties,
        final String errorCode,
        final List<String> errorArguments
    ) {
        super("className=" + objectClass.getSimpleName() + ", properties=" + properties);
        this.className = objectClass.getSimpleName();
        this.properties = properties;
        this.errorCode = errorCode;
        this.errorArguments = errorArguments;
    }

    protected AbstractException(
        final Class<?> objectClass,
        final String propertyName,
        final Object propertyValue,
        final String errorCode,
        final List<String> errorArguments
    ) {
        this(objectClass, Map.of(propertyName, propertyValue), errorCode, errorArguments);
    }
}
