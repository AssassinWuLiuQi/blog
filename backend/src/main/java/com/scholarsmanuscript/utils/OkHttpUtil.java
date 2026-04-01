package com.scholarsmanuscript.utils;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OkHttpUtil {

    private static OkHttpClient okHttpClient;

    @Autowired
    public void setOkHttpClient(OkHttpClient okHttpClient) {
        OkHttpUtil.okHttpClient = okHttpClient;
    }

    public static <T> T execute(Request request, Class<T> clazz) throws IOException {
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            String body = response.body() != null ? response.body().string() : "";
            return JSON.parseObject(body, clazz);
        }
    }

    public static void executeStream(Request request, OkHttpStreamCallback callback) {
        OkHttpClient client = okHttpClient;

        EventSource.Factory factory = EventSources.createFactory(client);

        EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                log.info("SSE connected");
                callback.onOpen(eventSource);
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                callback.onEvent(eventSource, id, type, data);
            }

            @Override
            public void onClosed(EventSource eventSource) {
                callback.onClosed(eventSource);
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                callback.onFailure(eventSource, t);
            }
        });
    }

    public interface OkHttpStreamCallback {
        void onOpen(EventSource eventSource);
        void onEvent(EventSource eventSource, String id, String type, String data);
        void onFailure(EventSource eventSource, Throwable t);
        void onClosed(EventSource eventSource);
    }
}
