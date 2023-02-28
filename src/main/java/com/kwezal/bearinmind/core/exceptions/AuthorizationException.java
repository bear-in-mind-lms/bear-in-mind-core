package com.kwezal.bearinmind.core.exceptions;

import java.util.List;
import java.util.Map;

public class AuthorizationException extends AbstractException {

    public AuthorizationException(
        final Class<?> objectClass,
        final Map<String, Object> properties,
        final String errorCode,
        final List<String> errorArguments
    ) {
        super(objectClass, properties, errorCode, errorArguments);
    }

    public AuthorizationException(final Class<?> objectClass, final Map<String, Object> properties, final String errorCode) {
        this(objectClass, properties, errorCode, null);
    }

    public AuthorizationException(
        final Class<?> objectClass,
        final String propertyName,
        final String propertyValue,
        final String errorCode,
        final List<String> errorArguments
    ) {
        super(objectClass, propertyName, propertyValue, errorCode, errorArguments);
    }

    public AuthorizationException(
        final Class<?> objectClass,
        final String propertyName,
        final String propertyValue,
        final String errorCode
    ) {
        this(objectClass, propertyName, propertyValue, errorCode, null);
    }

    public AuthorizationException(final Class<?> objectClass, final String propertyName, final String propertyValue) {
        this(objectClass, propertyName, propertyValue, ErrorCode.AUTHORIZATION_ERROR, null);
    }
}
