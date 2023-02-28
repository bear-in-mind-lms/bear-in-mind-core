package com.kwezal.bearinmind.core.exceptions;

import java.util.Map;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends AbstractException {

    public ResourceNotFoundException(final Class<?> objectClass, final Map<String, Object> properties) {
        super(objectClass, properties, ErrorCode.NOT_FOUND, null);
    }

    public ResourceNotFoundException(final Class<?> objectClass, final String propertyName, final Object propertyValue) {
        super(objectClass, propertyName, propertyValue, ErrorCode.NOT_FOUND, null);
    }
}
