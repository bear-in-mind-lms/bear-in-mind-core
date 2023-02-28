package com.kwezal.bearinmind.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class BearInMindCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BearInMindCoreApplication.class, args);
    }
}
