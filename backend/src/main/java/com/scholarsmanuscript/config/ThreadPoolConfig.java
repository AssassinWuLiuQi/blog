package com.scholarsmanuscript.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class ThreadPoolConfig {

    private final AppConfig appConfig;

    @Bean
    public Executor sseExecutor() {
        AppConfig.ThreadPool tp = appConfig.getThreadPool();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                tp.getCoreSize(),
                tp.getMaxSize(),
                tp.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>()
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
