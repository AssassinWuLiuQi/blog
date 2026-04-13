package com.scholarsmanuscript.config;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OkHttpConfig {

    private final AppConfig appConfig;

    @Bean
    public OkHttpClient okHttpClient() {
        AppConfig.Http http = appConfig.getHttp();
        return new OkHttpClient.Builder()
                .connectTimeout(http.getConnectTimeoutSeconds(), java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(http.getReadTimeoutSeconds(), java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(http.getWriteTimeoutSeconds(), java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }
}
