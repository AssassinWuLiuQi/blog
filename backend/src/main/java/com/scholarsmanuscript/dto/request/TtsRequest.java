package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * MiniMax TTS 语音合成请求参数
 *
 * @see <a href="https://platform.minimaxi.com/docs/api-reference/speech-t2a-http">MiniMax TTS API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsRequest {

    /**
     * 待合成的文本内容
     * - 最大10000字符
     * - 文本中的 Latex 公式需用 $$ 包裹（需开启 latex_read）
     */
    @NotBlank(message = "Text is required")
    private String text;

    // ==================== voice_setting 音色设置 ====================

    /**
     * 音色编号
     * - 支持系统音色、复刻音色及文生音色
     * - 默认: male-qn-qingse
     */
    @Builder.Default
    private String voiceId = "male-qn-qingse";

    /**
     * 语速
     * - 取值范围: [0.5, 2]
     * - 默认: 1.0
     */
    @Builder.Default
    @DecimalMin("0.5")
    @DecimalMax("2.0")
    private Float speed = 1.0f;

    /**
     * 音量
     * - 取值范围: (0, 10]
     * - 默认: 1
     */
    @Builder.Default
    @DecimalMin(value = "0.01", message = "Volume must be greater than 0")
    @DecimalMax("10.0")
    private Integer vol = 1;

    /**
     * 语调
     * - 取值范围: [-12, 12]
     * - 默认: 0
     */
    @Builder.Default
    @Min(-12)
    @Max(12)
    private Integer pitch = 0;

    /**
     * 情绪控制
     * - 可选值: happy, sad, angry, fearful, disgusted, surprised, calm, fluent, whisper
     * - 默认: happy
     */
    @Builder.Default
    private String emotion = "happy";

    /**
     * 是否启用中英文文本规范化
     * - 默认: false
     */
    @Builder.Default
    private Boolean textNormalization = false;

    /**
     * 是否朗读 Latex 公式
     * - 需在公式首尾加 $$ 包裹
     * - 默认: false
     */
    @Builder.Default
    private Boolean latexRead = false;

    // ==================== audio_setting 音频设置 ====================

    /**
     * 采样率
     * - 可选值: 8000, 16000, 22050, 24000, 32000, 44100
     * - 默认: 32000
     */
    private Integer sampleRate = 32000;

    /**
     * 比特率
     * - 可选值: 32000, 64000, 128000, 256000
     * - 默认: 128000
     */
    private Integer bitrate = 128000;

    /**
     * 音频格式
     * - 可选值: mp3, pcm, flac, wav（wav仅非流式）
     * - 默认: pcm
     */
    @Builder.Default
    private String format = "pcm";

    /**
     * 声道数
     * - 可选值: 1（单声道）, 2（双声道）
     * - 默认: 1
     */
    @Builder.Default
    private Integer channel = 1;

    /**
     * 恒定比特率控制（仅流式mp3生效）
     * - 默认: false
     */
    @Builder.Default
    private Boolean forceCbr = false;

    // ==================== voice_modify 声音效果器 ====================

    /**
     * 音高调整
     * - 取值范围: [-100, 100]
     * - 接近-100更低沉，接近100更明亮
     */
    private Integer voicePitch;

    /**
     * 强度调整
     * - 取值范围: [-100, 100]
     * - 接近-100更刚劲，接近100更轻柔
     */
    private Integer voiceIntensity;

    /**
     * 音色调整
     * - 取值范围: [-100, 100]
     * - 接近-100更浑厚，接近100更清脆
     */
    private Integer voiceTimbre;

    /**
     * 音效
     * - 可选值: spacious_echo（空旷回音）, auditorium_echo（礼堂广播）,
     *           lofi_telephone（电话失真）, robotic（电音）
     */
    private String soundEffects;

    // ==================== 其他高级参数 ====================

    /**
     * 发音字典
     * - 用于特殊文字/符号的注音或发音替换
     * - 中文声调用数字表示（1-4声，5轻声）
     * - 示例: ["燕少飞/(yan4)(shao3)(fei1)", "omg/oh my god"]
     */
    private String[] pronunciationDict;

    /**
     * 混合音色权重
     * - 用于混合多个音色的声音
     */
    private Object[] timbreWeights;

    /**
     * 语言增强
     * - 用于增强特定语言的合成效果
     */
    private String languageBoost;

    /**
     * 是否开启字幕
     * - 默认: false
     */
    @Builder.Default
    private Boolean subtitleEnable = false;

    /**
     * 输出格式
     * - 可选值: url（返回下载链接）, hex（返回hex编码音频数据）
     * - 流式模式建议使用 hex
     * - 默认: hex
     */
    @Builder.Default
    private String outputFormat = "hex";

    /**
     * 是否添加AIGC水印
     * - 默认: false
     */
    @Builder.Default
    private Boolean aigcWatermark = false;

    /**
     * 是否流式输出
     * - 默认: true（当前实现使用流式）
     */
    @Builder.Default
    private Boolean stream = true;
}
