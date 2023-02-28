package com.kwezal.bearinmind.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ApplicationConfig {

    @Value("${application.locale}")
    String applicationLocale;
}
