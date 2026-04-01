package com.scholarsmanuscript.dto.response;

import lombok.*;
import java.util.List;

/**
 * MiniMax 音色响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceResponse {

    /**
     * 音色唯一标识符
     */
    private String voiceId;

    /**
     * 音色名称
     */
    private String voiceName;

    /**
     * 音色描述列表
     */
    private List<String> description;

    /**
     * 创建时间，格式: yyyy-MM-dd HH:mm:ss
     */
    private String createdTime;

    /**
     * 音色类型
     * - system_voice: 系统预置音色
     * - voice_cloning: 复刻音色（需实名认证）
     * - voice_generation: 文生音色
     */
    private String type;
}
