package com.scholarsmanuscript.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private ThreadPool threadPool = new ThreadPool();
    private Http http = new Http();

    @Getter
    @Setter
    public static class ThreadPool {
        private int coreSize = 10;
        private int maxSize = 100;
        private long keepAliveSeconds = 60;
    }

    @Getter
    @Setter
    public static class Http {
        private int connectTimeoutSeconds = 30;
        private int readTimeoutSeconds = 0;
        private int writeTimeoutSeconds = 60;
    }
}
