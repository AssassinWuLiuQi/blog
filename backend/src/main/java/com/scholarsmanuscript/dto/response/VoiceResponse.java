package com.scholarsmanuscript.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceResponse {

    private String voiceId;
    private String voiceName;
    private List<String> description;
    private String createdTime;
    private String type;  // system_voice, voice_cloning, voice_generation
}
