/**
 * TTS 语音合成请求参数
 * @see https://platform.minimaxi.com/docs/api-reference/speech-t2a-http
 */
export interface TtsRequest {
  // 常用参数
  /** 待合成的文本内容 */
  text: string
  /** 音色编号 */
  voiceId: string
  /** 语速 [0.5, 2]，默认 1.0 */
  speed?: number
  /** 情绪控制: happy, sad, angry, fearful, disgusted, surprised, calm, fluent, whisper */
  emotion?: string
  /** 音频格式: pcm, mp3, flac, wav（wav仅非流式），默认 pcm */
  format?: string

  // 高级参数
  /** 音量 (0, 10]，默认 1 */
  vol?: number
  /** 语调 [-12, 12]，默认 0 */
  pitch?: number
  /** 声道数: 1=单声道, 2=双声道，默认 1 */
  channel?: number
  /** 恒定比特率控制（仅流式mp3生效），默认 false */
  forceCbr?: boolean
  /** 是否启用中英文文本规范化，默认 false */
  textNormalization?: boolean
  /** 是否朗读 Latex 公式（需在公式首尾加$$），默认 false */
  latexRead?: boolean
  /** 声音效果器-音高 [-100, 100]，负=低沉，正=明亮 */
  voicePitch?: number
  /** 声音效果器-强度 [-100, 100]，负=刚劲，正=轻柔 */
  voiceIntensity?: number
  /** 声音效果器-音色 [-100, 100]，负=浑厚，正=清脆 */
  voiceTimbre?: number
  /** 音效: spacious_echo, auditorium_echo, lofi_telephone, robotic */
  soundEffects?: string
  /** 是否开启字幕，默认 false */
  subtitleEnable?: boolean
  /** 是否添加AIGC水印，默认 false */
  aigcWatermark?: boolean
}

/**
 * 音色响应数据
 */
export interface VoiceResponse {
  voiceId: string
  voiceName: string
  description: string[]
  createdTime: string
  /** 音色类型: system_voice, voice_cloning, voice_generation */
  type: string
}

/**
 * SSE 消息格式
 */
export interface SseMessage {
  step: number
  message?: string
  data?: string
}

/**
 * Web Audio 配置
 */
export interface TTSOptions {
  sampleRate?: number
  bufferSize?: number
  channels?: number
}
