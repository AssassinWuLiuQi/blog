package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MusicGenerationRequest {

    private String model = "music-2.6";

    @NotBlank(message = "prompt is required")
    private String prompt;

    private String lyrics;

    private boolean isInstrumental = false;
}
