package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

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

    @Min(value = 1, message = "Number of images must be at least 1")
    @Max(value = 9, message = "Number of images must not exceed 9")
    @Builder.Default
    private Integer n = 1;

    @Builder.Default
    private String responseFormat = "url";

    @Builder.Default
    private Boolean promptOptimizer = false;

    @Builder.Default
    private Boolean aigcWatermark = false;

    /**
     * Subject reference for image-to-image generation.
     * Contains reference image(s) to maintain character/object consistency.
     */
    private List<SubjectReference> subjectReference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectReference {
        /**
         * Type of subject reference. Currently only "character" is supported.
         */
        @Builder.Default
        private String type = "character";

        /**
         * Reference image URL or base64 encoded image data.
         * If base64, must be prefixed with "data:image/*;base64,"
         */
        private String imageFile;
    }
}