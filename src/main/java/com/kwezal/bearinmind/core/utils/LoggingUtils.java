package com.kwezal.bearinmind.core.utils;

import java.util.Map;
import java.util.StringJoiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingUtils {

    public static final String GENERIC_EXCEPTION_MESSAGE = "{}: {}";

    public static final String JWT_PARSING_EXCEPTION_MESSAGE = "Attempt to authenticate with invalid JWT token: {}";

    private static final String ARGUMENTS_DELIMITER = ", ";

    private static final String ARGUMENTS_VALIDATION_FAILED = "Arguments validation failed: ";
    private static final String AUTHORIZATION_EXCEPTION_FOR_CLASS = "Authorization exception for class ";
    private static final String INVALID_REQUEST_DATA_FOR_CLASS = "Invalid request data for class ";

    private static final String PREFIX_AND_PROPERTIES = " and properties ";
    private static final String PREFIX_FOR = " for ";

    public static String resourceNotFound(final String entityName, final Map<String, Object> arguments) {
        final var sb = new StringBuilder(entityName).append(" not found");

        appendArguments(sb, PREFIX_FOR, arguments);

        return sb.toString();
    }

    public static String invalidRequestDataException(final String entityName, final Map<String, Object> arguments) {
        final var sb = new StringBuilder(INVALID_REQUEST_DATA_FOR_CLASS).append(entityName);

        appendArguments(sb, PREFIX_AND_PROPERTIES, arguments);

        return sb.toString();
    }

    public static String methodArgumentNotValidException() {
        return ARGUMENTS_VALIDATION_FAILED + "methodName='{}', parameterName='{}', fields={}, messages={}";
    }

    public static String bindException() {
        return ARGUMENTS_VALIDATION_FAILED + "className='{}', fields={}, messages={}";
    }

    public static String authorizationException(final String entityName, final Map<String, Object> arguments) {
        final var sb = new StringBuilder(AUTHORIZATION_EXCEPTION_FOR_CLASS).append(entityName);

        appendArguments(sb, PREFIX_AND_PROPERTIES, arguments);

        return sb.toString();
    }

    private static void appendArguments(final StringBuilder sb, final String prefix, final Map<String, Object> arguments) {
        if (!arguments.isEmpty()) {
            sb.append(prefix);

            final var sj = new StringJoiner(ARGUMENTS_DELIMITER);
            for (final var entry : arguments.entrySet()) {
                sj.add(entry.getKey() + "='" + entry.getValue() + "'");
            }

            sb.append(sj);
        }
    }
}
