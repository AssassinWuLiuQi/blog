package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LyricsGenerationRequest {

    @NotBlank(message = "prompt is required")
    private String prompt;

    private String mode = "write_full_song";
}
