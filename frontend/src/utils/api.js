export function authHeaders() {
  const token = localStorage.getItem('accessToken')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

export async function fetchWithAuth(url, options = {}) {
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
