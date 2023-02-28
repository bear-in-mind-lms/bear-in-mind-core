package com.kwezal.bearinmind.core.logging;

import static java.util.Objects.isNull;

import java.util.StringJoiner;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    private static final String PROCESSING_REQUEST_WITH_ARGUMENTS = "Processing request '{} {}' with arguments: {}";
    private static final String PROCESSING_REQUEST_WITHOUT_ARGUMENTS = "Processing request '{} {}'";

    private static final String REQUEST_SUCCESS_WITHOUT_VALUE = "Request '{} {}' completed successfully";
    private static final String REQUEST_SUCCESS_WITH_VALUE = "Request '{} {}' successfully returned '{}'";

    @Before("@within(com.kwezal.bearinmind.core.logging.ControllerLogging)")
    public void logRequestStart(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            final var controllerPath = getControllerBasePath(joinPoint);
            final var endpointPath = getEndpointPath(methodSignature);
            final var absoluteEndpointPath = controllerPath + endpointPath.path;
            final var arguments = getArguments(joinPoint, methodSignature);

            if (arguments.isEmpty()) {
                log.info(PROCESSING_REQUEST_WITHOUT_ARGUMENTS, endpointPath.method, absoluteEndpointPath);
            } else {
                log.info(PROCESSING_REQUEST_WITH_ARGUMENTS, endpointPath.method, absoluteEndpointPath, arguments);
            }
        }
    }

    @AfterReturning(value = "@within(com.kwezal.bearinmind.core.logging.ControllerLogging)", returning = "value")
    public void logRequestSuccess(JoinPoint joinPoint, Object value) {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            final var controllerPath = getControllerBasePath(joinPoint);
            final var endpointPath = getEndpointPath(methodSignature);

            if (isNull(value)) {
                log.info(REQUEST_SUCCESS_WITHOUT_VALUE, endpointPath.method, controllerPath + endpointPath.path);
            } else {
                log.info(REQUEST_SUCCESS_WITH_VALUE, endpointPath.method, controllerPath + endpointPath.path, value);
            }
        }
    }

    private String getControllerBasePath(JoinPoint joinPoint) {
        for (final var annotation : joinPoint.getTarget().getClass().getAnnotations()) {
            if (annotation instanceof ControllerLogging controllerLogging) {
                return controllerLogging.value();
            }
        }

        return "";
    }

    private record EndpointMethodAndPath(String method, String path) {}

    private EndpointMethodAndPath getEndpointPath(MethodSignature methodSignature) {
        for (final var annotation : methodSignature.getMethod().getAnnotations()) {
            if (annotation instanceof GetMapping mapping) {
                return new EndpointMethodAndPath("GET", getMappingPath(mapping.value()));
            } else if (annotation instanceof PostMapping mapping) {
                return new EndpointMethodAndPath("POST", getMappingPath(mapping.value()));
            } else if (annotation instanceof PutMapping mapping) {
                return new EndpointMethodAndPath("PUT", getMappingPath(mapping.value()));
            } else if (annotation instanceof DeleteMapping mapping) {
                return new EndpointMethodAndPath("DELETE", getMappingPath(mapping.value()));
            }
        }

        return new EndpointMethodAndPath("", "");
    }

    private String getMappingPath(String[] mappingValue) {
        return mappingValue.length == 0 ? "" : mappingValue[0];
    }

    private String getArguments(JoinPoint joinPoint, MethodSignature methodSignature) {
        final var params = methodSignature.getParameterNames();
        final var args = joinPoint.getArgs();
        final var joiner = new StringJoiner(", ");
        for (var i = 0; i < params.length; i++) {
            joiner.add(params[i] + "=" + args[i]);
        }

        return joiner.toString();
    }
}
