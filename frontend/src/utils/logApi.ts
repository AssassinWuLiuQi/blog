import { fetchWithAuth } from './api'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'
const API_SUCCESS_CODE = 200

export interface OperationLog {
  id: number
  userId: number | null
  username: string
  operation: string | null
  methodName: string
  className: string
  httpMethod: string
  requestUri: string
  parameters: string | null
  executionTime: number
  ip: string | null
  success: boolean
  errorMessage: string | null
  errorTrace: string | null
  createdAt: string
}

export interface LogListResponse {
  content: OperationLog[]
  totalPages: number
  totalElements: number
}

export async function fetchLogs(
  page: number,
  size: number,
  success?: boolean | null
): Promise<LogListResponse> {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  })
  if (success !== null && success !== undefined) {
    params.append('success', success.toString())
  }

  const response = await fetchWithAuth(`${API_BASE}/logs?${params}`, {
    headers: {
      'Content-Type': 'application/json'
    }
  })

  const result = await response.json()
  if (!response.ok || result.code !== API_SUCCESS_CODE) {
    throw new Error(result.message || 'Failed to fetch logs')
  }

  return result.data
}
