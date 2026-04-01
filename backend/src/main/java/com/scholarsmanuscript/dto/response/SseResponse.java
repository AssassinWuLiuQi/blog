package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseResponse {

    public static final int STEP_INIT_MIN = 0;
    public static final int STEP_INIT_MAX = 99;
    public static final int STEP_DATA_MIN = 100;
    public static final int STEP_DATA_MAX = 199;
    public static final int STEP_ERROR_MIN = 400;
    public static final int STEP_ERROR_MAX = 499;
    public static final int STEP_COMPLETE_MIN = 1000;
    public static final int STEP_COMPLETE_MAX = 1099;

    private int step;
    private String message;
    private Object data;

    // init: step 0-99
    public static SseResponse init(String message) {
        return init(message, STEP_INIT_MIN);
    }

    public static SseResponse init(String message, int step) {
        if (step < STEP_INIT_MIN || step > STEP_INIT_MAX) {
            throw new IllegalArgumentException("init step must be between " + STEP_INIT_MIN + " and " + STEP_INIT_MAX);
        }
        return SseResponse.builder()
                .step(step)
                .message(message)
                .build();
    }

    // data: step 100-199
    public static SseResponse data(Object data) {
        return data(data, STEP_DATA_MIN);
    }

    public static SseResponse data(Object data, int step) {
        if (step < STEP_DATA_MIN || step > STEP_DATA_MAX) {
            throw new IllegalArgumentException("data step must be between " + STEP_DATA_MIN + " and " + STEP_DATA_MAX);
        }
        return SseResponse.builder()
                .step(step)
                .message("success")
                .data(data)
                .build();
    }

    // error: step 400-499
    public static SseResponse error(String message) {
        return error(message, STEP_ERROR_MIN);
    }

    public static SseResponse error(String message, int step) {
        if (step < STEP_ERROR_MIN || step > STEP_ERROR_MAX) {
            throw new IllegalArgumentException("error step must be between " + STEP_ERROR_MIN + " and " + STEP_ERROR_MAX);
        }
        return SseResponse.builder()
                .step(step)
                .message(message)
                .build();
    }

    // complete: step 1000-1099
    public static SseResponse complete() {
        return complete(STEP_COMPLETE_MIN);
    }

    public static SseResponse complete(int step) {
        if (step < STEP_COMPLETE_MIN || step > STEP_COMPLETE_MAX) {
            throw new IllegalArgumentException("complete step must be between " + STEP_COMPLETE_MIN + " and " + STEP_COMPLETE_MAX);
        }
        return SseResponse.builder()
                .step(step)
                .message("complete")
                .build();
    }
}
