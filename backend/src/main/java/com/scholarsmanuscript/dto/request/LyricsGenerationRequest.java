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
public class LyricsGenerationRequest {

    @NotBlank(message = "prompt is required")
    private String prompt;

    @Builder.Default
    private String mode = "write_full_song";
}
