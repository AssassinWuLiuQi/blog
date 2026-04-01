package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsRequest {

    @NotBlank(message = "Text is required")
    private String text;

    @Builder.Default
    private String voiceId = "male-qn-qingse";

    @Builder.Default
    private Float speed = 1.0f;

    @Builder.Default
    private Integer vol = 1;

    @Builder.Default
    private Integer pitch = 0;

    @Builder.Default
    private String emotion = "happy";

    @Builder.Default
    private Integer sampleRate = 32000;

    @Builder.Default
    private Integer bitrate = 128000;

    @Builder.Default
    private String format = "pcm";

    @Builder.Default
    private Integer channel = 1;
}
