import { fetchWithAuth } from './api'
import type { ImageGenerationRequest, ImageGenerationResponse } from '@/types/image'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'
const API_SUCCESS_CODE = 200

export async function generateImage(
  request: ImageGenerationRequest
): Promise<ImageGenerationResponse> {
  const response = await fetchWithAuth(`${API_BASE}/image/generate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
  })

  const result = await response.json()
  if (!response.ok) {
    throw new Error(result.message || 'Image generation failed')
  }

  return result.data
}

export interface ImageHistoryItem {
  id: number
  title: string
  date: string
  description: string
  thumbnail: string
  imageUrls: string[]
  model: string
  aspectRatio: string
  style: string
  successCount: number
  failedCount: number
  createdAt: string
}

const formatDate = (dateStr: string): string => {
  const d = new Date(dateStr)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export async function fetchImageHistory(page: number, size: number): Promise<ImageHistoryItem[]> {
  try {
    const res = await fetchWithAuth(`${API_BASE}/image/history?page=${page}&size=${size}`, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    if (!res.ok) {
      console.error('Failed to fetch history:', res.status, res.statusText)
      return []
    }
    const json = await res.json()
    if (json.code !== API_SUCCESS_CODE) {
      console.error('Failed to fetch history:', json.message)
      return []
    }
    return json.data.content.map((item: any) => ({
      id: item.id,
      title: item.prompt.length > 30 ? item.prompt.substring(0, 30) + '...' : item.prompt,
      date: formatDate(item.createdAt),
      description: item.prompt,
      thumbnail: item.imageUrls && item.imageUrls.length > 0 ? item.imageUrls[0] : '',
      imageUrls: item.imageUrls || [],
      model: item.model,
      aspectRatio: item.aspectRatio,
      style: item.style,
      successCount: item.successCount,
      failedCount: item.failedCount,
      createdAt: item.createdAt
    }))
  } catch (error) {
    console.error('Failed to fetch history:', error)
    return []
  }
}
