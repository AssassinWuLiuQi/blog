import { fetchEventSource, EventSourceMessage } from '@microsoft/fetch-event-source'

function getAuthHeader(): Record<string, string> {
  const token = localStorage.getItem('accessToken')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

interface SSEOptions {
  method?: string
  headers?: Record<string, string>
  body?: unknown
  onMessage?: (data: unknown) => void
  onOpen?: () => void
  onClose?: () => void
  onError?: (err: Error) => void
}

interface SSERawOptions {
  method?: string
  headers?: Record<string, string>
  body?: unknown
  onMessage?: (msg: EventSourceMessage) => void
  onOpen?: () => void
  onClose?: () => void
  onError?: (err: Error) => void
}

export function createSSEStream(url: string, options: SSEOptions = {}): AbortController {
  const {
    method = 'POST',
    headers = {},
    body,
    onMessage = () => { /* noop */ },
    onOpen = () => { /* noop */ },
    onClose = () => { /* noop */ },
    onError = () => { /* noop */ }
  } = options

  const ctrl = new AbortController()

  fetchEventSource(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
      ...headers
    },
    signal: ctrl.signal,
    body: JSON.stringify(body),
    onmessage: (msg: { data?: string }) => {
      if (msg.data) {
        try {
          const data = JSON.parse(msg.data)
          onMessage(data)
        } catch {
          onMessage(msg.data)
        }
      }
    },
    onopen: async () => { onOpen() },
    onclose: async () => { onClose() },
    onerror: (err) => { onError(err) }
  })

  return ctrl
}

export function createSSERawStream(url: string, options: SSERawOptions = {}): AbortController {
  const {
    method = 'POST',
    headers = {},
    body,
    onMessage = () => { /* noop */ },
    onOpen = () => { /* noop */ },
    onClose = () => { /* noop */ },
    onError = () => { /* noop */ }
  } = options

  const ctrl = new AbortController()

  fetchEventSource(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
      ...headers
    },
    signal: ctrl.signal,
    body: typeof body === 'string' ? body : JSON.stringify(body),
    onmessage: onMessage,
    onopen: async () => { onOpen() },
    onclose: async () => { onClose() },
    onerror: (err) => { onError(err) }
  })

  return ctrl
}
