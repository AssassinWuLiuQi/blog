// frontend/src/types/music.ts

export interface LyricsGenerationRequest {
  prompt: string
  mode?: string
}

export interface LyricsGenerationResponse {
  lyrics: string | null
  statusCode: number
  statusMsg: string
}

export interface MusicGenerationRequest {
  model: string
  prompt: string
  lyrics?: string
  isInstrumental: boolean
}

export interface MusicGenerationResponse {
  audioUrl: string | null
  statusCode: number
  statusMsg: string
  traceId: string | null
}

export interface MusicHistoryItem {
  id: number
  prompt: string
  lyrics: string | null
  audioUrl: string | null
  model: string
  isInstrumental: boolean
  statusCode: number
  statusMsg: string
  createdAt: string
  // computed display fields
  displayDate: string
  displayTitle: string
}
