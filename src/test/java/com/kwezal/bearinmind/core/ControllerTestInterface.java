package com.kwezal.bearinmind.core;

import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public interface ControllerTestInterface {
    String urlBase();

    default String url() {
        return urlBase();
    }

    default UriBuilder url(UriBuilder builder, String path) {
        return builder.path(urlBase() + path);
    }

    default UriBuilder url(String path) {
        return UriComponentsBuilder.fromPath(urlBase() + path);
    }
}
