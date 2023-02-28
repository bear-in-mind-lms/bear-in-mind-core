package com.kwezal.bearinmind.core.exceptions;

import java.util.List;
import java.util.Map;

public class ForbiddenException extends AbstractException {

    public ForbiddenException(
        final Class<?> objectClass,
        final String propertyName,
        final String propertyValue,
        final String errorCode,
        final List<String> errorArguments
    ) {
        super(objectClass, propertyName, propertyValue, errorCode, errorArguments);
    }

    public ForbiddenException(
        final Class<?> objectClass,
        final String propertyName,
        final String propertyValue,
        final String errorCode
    ) {
        this(objectClass, propertyName, propertyValue, errorCode, null);
    }

    public ForbiddenException(Class<?> objectClass, Map<String, Object> properties, List<String> errorArguments) {
        super(objectClass, properties, ErrorCode.FORBIDDEN, errorArguments);
    }
}
