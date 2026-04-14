package com.scholarsmanuscript.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LyricsGenerationResponse {

    private String lyrics;
    private Integer statusCode;
    private String statusMsg;
}
