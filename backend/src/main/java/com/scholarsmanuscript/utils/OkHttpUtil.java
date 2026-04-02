package com.scholarsmanuscript.utils;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OkHttpUtil {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * SSE 流式回调接口
     */
    public interface OkHttpStreamCallback {
        void onOpen(EventSource eventSource);
        void onEvent(EventSource eventSource, String id, String type, String data);
        void onFailure(EventSource eventSource, Throwable t);
        void onClosed(EventSource eventSource);
    }

    /**
     * 执行 SSE 流式请求
     */
    public static void executeStream(Request request, OkHttpStreamCallback callback) {
        EventSource.Factory factory = EventSources.createFactory(CLIENT);

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
                log.error("SSE failure: {}", t.getMessage());
                callback.onFailure(eventSource, t);
            }
        });
    }

    /**
     * 执行普通 HTTP 请求
     */
    public static <T> T execute(Request request, Class<T> responseClass) throws IOException {
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                return null;
            }

            String json = body.string();
            return parseJson(json, responseClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T parseJson(String json, Class<T> responseClass) {
        if (responseClass == Map.class) {
            return (T) JSON.parseObject(json, Map.class);
        }
        return JSON.parseObject(json, responseClass);
    }
}
