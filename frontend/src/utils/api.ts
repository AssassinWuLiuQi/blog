export function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('accessToken')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

export async function fetchWithAuth(url: string, options: RequestInit = {}): Promise<Response> {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      ...authHeaders()
    }
  })

  if (response.status === 403) {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    window.location.href = '/login'
  }

  return response
}
