package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationRequest {

    @NotBlank(message = "Prompt is required")
    @Size(max = 1500, message = "Prompt must not exceed 1500 characters")
    private String prompt;

    @Builder.Default
    private String model = "image-01";

    @Builder.Default
    private String aspectRatio = "1:1";

    @Builder.Default
    private Integer n = 1;

    @Builder.Default
    private String responseFormat = "url";

    @Builder.Default
    private Boolean promptOptimizer = false;

    @Builder.Default
    private Boolean aigcWatermark = false;
}
