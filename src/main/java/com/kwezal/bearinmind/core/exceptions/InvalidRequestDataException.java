package com.kwezal.bearinmind.core.exceptions;

import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class InvalidRequestDataException extends AbstractException {

    public InvalidRequestDataException(
        final Class<?> objectClass,
        final Map<String, Object> properties,
        final String errorCode,
        final List<String> errorArguments
    ) {
        super(objectClass, properties, errorCode, errorArguments);
    }

    public InvalidRequestDataException(final Class<?> objectClass, final Map<String, Object> properties) {
        super(objectClass, properties, ErrorCode.REQUEST_ARGUMENT_INVALID, null);
    }

    public InvalidRequestDataException(
        final Class<?> objectClass,
        final String propertyName,
        final Object propertyValue,
        final List<String> errorArguments
    ) {
        super(objectClass, propertyName, propertyValue, ErrorCode.REQUEST_ARGUMENT_INVALID, errorArguments);
    }

    public InvalidRequestDataException(final Class<?> objectClass, final String propertyName, final Object propertyValue) {
        this(objectClass, propertyName, propertyValue, null);
    }

    public InvalidRequestDataException(
        final Class<?> objectClass,
        final String propertyName,
        final Object propertyValue,
        final String errorCode,
        final List<String> errorArguments
    ) {
        super(objectClass, propertyName, propertyValue, errorCode, errorArguments);
    }
}
