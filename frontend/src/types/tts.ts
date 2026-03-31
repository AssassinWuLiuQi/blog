export interface TtsRequest {
  text: string
  voiceId: string
  speed?: number
  vol?: number
  pitch?: number
  emotion?: string
  sampleRate?: number
  bitrate?: number
  format?: string
  channel?: number
}

export interface VoiceResponse {
  voiceId: string
  voiceName: string
  description: string[]
  createdTime: string
  type: string
}

export interface SseMessage {
  step: number
  message?: string
  data?: string
}

export interface TTSOptions {
  sampleRate?: number
  bufferSize?: number
  channels?: number
}
