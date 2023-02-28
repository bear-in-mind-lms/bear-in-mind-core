package com.kwezal.bearinmind.core;

import static java.util.Objects.nonNull;

import com.kwezal.bearinmind.core.user.dto.UserRole;
import com.kwezal.bearinmind.core.utils.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;

public abstract class ControllerSecurityTest implements ControllerTestInterface {

    @Autowired
    protected WebTestClient webClient;

    @Autowired
    protected AuthHelper authHelper;

    protected WebTestClient.ResponseSpec request(HttpMethod method, String path, UserRole role, Object body) {
        final var request = webClient.method(method).uri(builder -> url(builder, path).build());

        if (nonNull(body)) {
            request.bodyValue(body);
        }

        return authHelper.asUserWithRole(role, request).exchange();
    }
}
