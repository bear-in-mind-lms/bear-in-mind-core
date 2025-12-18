package com.kwezal.bearinmind.core.config.security;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.user.model.UserCredentials;
import com.kwezal.bearinmind.exception.AuthorizationException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AccessManagementAspect {

    private final LoggedInUserService loggedInUserService;

    @Before("@annotation(com.kwezal.bearinmind.core.config.security.RoleRequired)")
    public void validRequiredRole(JoinPoint call) {
        MethodSignature signature = (MethodSignature) call.getSignature();
        Method method = signature.getMethod();

        RoleRequired annotation = method.getAnnotation(RoleRequired.class);

        if (Arrays.stream(annotation.value()).noneMatch(loggedInUserService.getLoggedInUserRoles()::contains)) {
            throw new AuthorizationException(
                UserCredentials.class,
                Map.of("role", loggedInUserService.getLoggedInUserRoles().toString())
            );
        }
    }
}
