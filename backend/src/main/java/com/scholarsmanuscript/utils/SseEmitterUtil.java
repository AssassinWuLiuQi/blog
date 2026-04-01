package com.scholarsmanuscript.utils;

import com.alibaba.fastjson2.JSON;
import com.scholarsmanuscript.dto.response.SseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
public class SseEmitterUtil {

    public static final String EVENT_MESSAGE = "message";

    public static void send(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data).name(EVENT_MESSAGE));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    public static void send(SseEmitter emitter, String data, String event) {
        try {
            emitter.send(SseEmitter.event().data(data).name(event));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    public static void send(SseEmitter emitter, SseResponse response) {
        send(emitter, JSON.toJSONString(response));
    }

    public static void send(SseEmitter emitter, SseResponse response, String event) {
        send(emitter, JSON.toJSONString(response), event);
    }

    public static void init(SseEmitter emitter, String message) {
        send(emitter, SseResponse.init(message));
    }

    public static void data(SseEmitter emitter, Object data) {
        send(emitter, SseResponse.data(data));
    }

    public static void error(SseEmitter emitter, String message) {
        send(emitter, SseResponse.error(message));
    }

    public static void complete(SseEmitter emitter) {
        send(emitter, SseResponse.complete());
        emitter.complete();
    }

    public static void completeWithError(SseEmitter emitter, Throwable t) {
        send(emitter, SseResponse.error(t.getMessage()));
        emitter.completeWithError(t);
    }
}
