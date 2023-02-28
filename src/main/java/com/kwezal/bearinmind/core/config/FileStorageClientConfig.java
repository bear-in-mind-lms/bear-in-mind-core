package com.kwezal.bearinmind.core.config;

import com.kwezal.bearinmind.filestorage.ApiClient;
import com.kwezal.bearinmind.filestorage.api.FileStorageClientApi;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@AllArgsConstructor
public class FileStorageClientConfig {

    private final RestTemplate restTemplate;

    @Bean
    public FileStorageClientApi fileStorageClientApi() {
        return new FileStorageClientApi(createApiClient());
    }

    private ApiClient createApiClient() {
        return new ApiClient(restTemplate);
    }
}
