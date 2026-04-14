package com.scholarsmanuscript.dto.response;

import com.scholarsmanuscript.entity.MusicGenerationHistory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MusicHistoryResponse {

    private Long id;
    private String prompt;
    private String lyrics;
    private String audioUrl;
    private String model;
    private boolean isInstrumental;
    private Integer statusCode;
    private String statusMsg;
    private LocalDateTime createdAt;

    public static MusicHistoryResponse fromEntity(MusicGenerationHistory entity) {
        return MusicHistoryResponse.builder()
                .id(entity.getId())
                .prompt(entity.getPrompt())
                .lyrics(entity.getLyrics())
                .audioUrl(entity.getAudioUrl())
                .model(entity.getModel())
                .isInstrumental(entity.isInstrumental())
                .statusCode(entity.getStatusCode())
                .statusMsg(entity.getStatusMsg())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
