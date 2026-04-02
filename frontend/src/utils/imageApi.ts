import { fetchWithAuth } from './api'
import type { ImageGenerationRequest, ImageGenerationResponse } from '@/types/image'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

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
