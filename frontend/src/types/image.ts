export interface ImageGenerationRequest {
  prompt: string
  model?: 'image-01' | 'image-01-live'
  aspectRatio?: string
  n?: number
  responseFormat?: 'url' | 'base64'
  promptOptimizer?: boolean
  aigcWatermark?: boolean
}

export interface ImageGenerationResponse {
  taskId: string
  imageUrls: string[]
  successCount: number
  failedCount: number
  statusCode: number
  statusMsg?: string
}
