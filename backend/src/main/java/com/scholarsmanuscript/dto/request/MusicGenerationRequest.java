package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicGenerationRequest {

    @Builder.Default
    private String model = "music-2.6";

    @NotBlank(message = "prompt is required")
    private String prompt;

    private String lyrics;

    @Builder.Default
    private boolean isInstrumental = false;
}
