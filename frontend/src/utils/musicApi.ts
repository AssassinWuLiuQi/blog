// frontend/src/utils/musicApi.ts
import { fetchWithAuth } from './api'
import type {
  LyricsGenerationRequest,
  LyricsGenerationResponse,
  MusicGenerationRequest,
  MusicGenerationResponse,
  MusicHistoryItem
} from '@/types/music'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

export async function generateLyrics(
  request: LyricsGenerationRequest
): Promise<LyricsGenerationResponse> {
  const response = await fetchWithAuth(`${API_BASE}/music/lyrics`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  const result = await response.json()
  if (!response.ok) {
    throw new Error(result.message || 'Lyrics generation failed')
  }
  return result.data
}

export async function generateMusic(
  request: MusicGenerationRequest
): Promise<MusicGenerationResponse> {
  const response = await fetchWithAuth(`${API_BASE}/music/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  const result = await response.json()
  if (!response.ok) {
    throw new Error(result.message || 'Music generation failed')
  }
  return result.data
}

const formatDate = (dateStr: string): string => {
  const d = new Date(dateStr)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export async function fetchMusicHistory(page: number, size: number): Promise<MusicHistoryItem[]> {
  try {
    const res = await fetchWithAuth(`${API_BASE}/music/history?page=${page}&size=${size}`)
    if (!res.ok) return []
    const json = await res.json()
    if (json.code !== 200) return []
    return json.data.content.map((item: any): MusicHistoryItem => ({
      id: item.id,
      prompt: item.prompt,
      lyrics: item.lyrics,
      audioUrl: item.audioUrl,
      model: item.model,
      isInstrumental: item.isInstrumental,
      statusCode: item.statusCode,
      statusMsg: item.statusMsg,
      createdAt: item.createdAt,
      displayDate: formatDate(item.createdAt),
      displayTitle: item.prompt.length > 30 ? item.prompt.substring(0, 30) + '...' : item.prompt
    }))
  } catch {
    return []
  }
}

export async function deleteMusicHistory(id: number): Promise<void> {
  await fetchWithAuth(`${API_BASE}/music/history/${id}`, { method: 'DELETE' })
}
