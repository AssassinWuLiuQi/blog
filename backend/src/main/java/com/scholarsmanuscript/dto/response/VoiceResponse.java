package com.scholarsmanuscript.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceResponse {

    private String voiceId;
    private String name;
    private String language;
    private String gender;
}
