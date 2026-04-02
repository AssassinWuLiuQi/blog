package com.scholarsmanuscript.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationResponse {

    private String taskId;

    private List<String> imageUrls;

    private Integer successCount;

    private Integer failedCount;

    private Integer statusCode;

    private String statusMsg;
}
