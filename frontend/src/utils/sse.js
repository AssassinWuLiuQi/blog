import { fetchEventSource } from '@microsoft/fetch-event-source'

// 获取 token
function getAuthHeader() {
  const token = localStorage.getItem('accessToken')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

/**
 * 封装 SSE 请求（自动带 token）
 * @param {string} url - 请求 URL
 * @param {object} options - 配置选项
 * @param {string} options.method - 请求方法，默认 POST
 * @param {object} options.headers - 请求头
 * @param {string|object} options.body - 请求体，会自动转为 JSON
 * @param {function} options.onMessage - 消息回调，参数为解析后的数据
 * @param {function} options.onOpen - 连接成功回调
 * @param {function} options.onClose - 连接关闭回调
 * @param {function} options.onError - 错误回调
 * @returns {AbortController} 用于取消请求
 */
export function createSSEStream(url, options = {}) {
  const {
    method = 'POST',
    headers = {},
    body,
    onMessage = () => {},
    onOpen = () => {},
    onClose = () => {},
    onError = () => {}
  } = options

  const ctrl = new AbortController()

  const fetchOptions = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
      ...headers
    },
    signal: ctrl.signal,
    body: typeof body === 'string' ? body : JSON.stringify(body),
    onMessage: (msg) => {
      if (msg.data) {
        try {
          const data = JSON.parse(msg.data)
          onMessage(data)
        } catch {
          // 如果不是 JSON，直接返回原始数据
          onMessage(msg.data)
        }
      }
    },
    onOpen,
    onClose,
    onError
  }

  fetchEventSource(url, fetchOptions)

  return ctrl
}

/**
 * 封装 SSE 请求，返回原始消息事件（自动带 token）
 * @param {string} url - 请求 URL
 * @param {object} options - 同上
 * @returns {AbortController}
 */
export function createSSERawStream(url, options = {}) {
  const {
    method = 'POST',
    headers = {},
    body,
    onMessage = () => {},
    onOpen = () => {},
    onClose = () => {},
    onError = () => {}
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
    onMessage,
    onOpen,
    onClose,
    onError
  })

  return ctrl
}
