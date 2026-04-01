package com.scholarsmanuscript.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
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

    public interface OkHttpStreamCallback {
        void onConnect(Call call);
        void onResponse(Call call, String data);
        void onFailure(Call call, IOException e);
        void onClose(Call call);
    }

    public static void executeStream(Request request, OkHttpStreamCallback callback) {
        CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("OkHttp stream failure: {}", e.getMessage());
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onConnect(call);
                if (!response.isSuccessful()) {
                    callback.onFailure(call, new IOException("Unexpected response code: " + response.code()));
                    return;
                }

                try (ResponseBody body = response.body()) {
                    if (body == null) {
                        callback.onClose(call);
                        return;
                    }

                    String data = body.string();
                    callback.onResponse(call, data);
                    callback.onClose(call);
                }
            }
        });
    }

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
            return (T) com.alibaba.fastjson2.JSON.parseObject(json, Map.class);
        }
        return com.alibaba.fastjson2.JSON.parseObject(json, responseClass);
    }
}
