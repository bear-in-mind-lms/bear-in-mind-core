package com.kwezal.bearinmind.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
public abstract class ControllerTest implements ControllerTestInterface {

    @Autowired
    protected WebTestClient webClient;
}
