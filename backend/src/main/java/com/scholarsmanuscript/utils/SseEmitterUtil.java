package com.scholarsmanuscript.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Component
public class SseEmitterUtil {

    public static void init(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("connected").data(message));
        } catch (IOException e) {
            log.error("Failed to send init event: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }

    public static void data(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().name("data").data(data));
        } catch (IOException e) {
            log.error("Failed to send data event: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }

    public static void completeWithError(SseEmitter emitter, Exception e) {
        log.error("SSE complete with error: {}", e.getMessage());
        emitter.completeWithError(e);
    }

    public static void complete(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("close").data("complete"));
            emitter.complete();
        } catch (IOException e) {
            log.error("Failed to send close event: {}", e.getMessage());
            emitter.complete();
        }
    }

    public static void error(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (IOException e) {
            log.error("Failed to send error event: {}", e.getMessage());
        }
        emitter.complete();
    }
}
