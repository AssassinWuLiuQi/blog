package com.scholarsmanuscript.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicGenerationResponse {

    private String audioUrl;
    private Integer statusCode;
    private String statusMsg;
    private String traceId;
}
