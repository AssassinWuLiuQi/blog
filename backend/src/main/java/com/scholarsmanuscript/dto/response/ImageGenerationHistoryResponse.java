package com.scholarsmanuscript.dto.response;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ImageGenerationHistoryResponse {

    private Long id;
    private String prompt;
    private List<String> imageUrls;
    private String model;
    private String aspectRatio;
    private String style;
    private Integer successCount;
    private Integer failedCount;
    private String taskId;
    private Integer statusCode;
    private String statusMsg;
    private LocalDateTime createdAt;

    public static ImageGenerationHistoryResponse fromEntity(com.scholarsmanuscript.entity.ImageGenerationHistory entity) {
        List<String> urls = null;
        if (entity.getImageUrls() != null) {
            try {
                urls = JSON.parseArray(entity.getImageUrls(), String.class);
            } catch (Exception e) {
                urls = List.of(entity.getImageUrls());
            }
        }

        return ImageGenerationHistoryResponse.builder()
                .id(entity.getId())
                .prompt(entity.getPrompt())
                .imageUrls(urls)
                .model(entity.getModel())
                .aspectRatio(entity.getAspectRatio())
                .style(entity.getStyle())
                .successCount(entity.getSuccessCount())
                .failedCount(entity.getFailedCount())
                .taskId(entity.getTaskId())
                .statusCode(entity.getStatusCode())
                .statusMsg(entity.getStatusMsg())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
