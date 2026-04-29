import { ref, onUnmounted } from 'vue'
import { createSSERawStream } from '@/utils/sse'
import type { TtsRequest } from '@/types/tts'

const STEP_DATA = 100

// 目标缓冲采样点数
const TARGET_BUFFER_SAMPLES = 8192
const INITIAL_BUFFER_SIZE = 65536 // 64K samples initial, grows exponentially

export function useTTS(options: { sampleRate?: number; bufferSize?: number; channels?: number } = {}) {
  const {
    sampleRate = 32000,
    bufferSize = 8192,
    channels = 1
  } = options

  const audioContext = ref<AudioContext | null>(null)
  const processor = ref<ScriptProcessorNode | null>(null)
  const pcmQueue = ref<Float32Array[]>([])
  const isPlaying = ref(false)
  const currentController = ref<AbortController | null>(null)

  // 预分配的可增长 Float32 缓冲区 + 有效长度追踪
  let float32Buf = new Float32Array(INITIAL_BUFFER_SIZE)
  let bufLen = 0
  let streamEnded = false

  const initAudio = (): void => {
    if (audioContext.value) return
    audioContext.value = new AudioContext({ sampleRate })
    processor.value = audioContext.value.createScriptProcessor(bufferSize, channels, channels)
    processor.value.connect(audioContext.value.destination)

    processor.value.onaudioprocess = (e) => {
      const output = e.outputBuffer.getChannelData(0)
      if (pcmQueue.value.length === 0) {
        output.fill(0)
        // 流结束且队列为空时停止
        if (streamEnded) stop()
        return
      }
      let offset = 0
      while (offset < bufferSize && pcmQueue.value.length > 0) {
        const chunk = pcmQueue.value[0]
        const remaining = bufferSize - offset
        if (chunk.length <= remaining) {
          output.set(chunk, offset)
          offset += chunk.length
          pcmQueue.value.shift()
        } else {
          output.set(chunk.subarray(0, remaining), offset)
          pcmQueue.value[0] = chunk.subarray(remaining)
          offset += remaining
        }
      }
    }
  }

  const stop = (): void => {
    if (currentController.value) {
      currentController.value.abort()
      currentController.value = null
    }
    pcmQueue.value = []
    bufLen = 0
    streamEnded = false
    isPlaying.value = false
  }

  /**
   * 将 hex 字符串转换为 Float32Array (PCM16 → Float32)
   */
  const hexToFloat32 = (hexString: string, isLittleEndian: boolean = false): Float32Array | null => {
    if (hexString.length % 2 !== 0) {
      console.warn('[TTS] Odd-length hex received, dropping last char')
      hexString = hexString.slice(0, -1)
      if (hexString.length === 0) return null
    }
    const bytes = new Uint8Array(hexString.length / 2)
    for (let i = 0; i < hexString.length; i += 2) {
      bytes[i / 2] = parseInt(hexString.substring(i, i + 2), 16)
    }
    const sampleCount = bytes.length / 2
    const float32 = new Float32Array(sampleCount)
    const view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength)
    for (let i = 0; i < sampleCount; i++) {
      float32[i] = view.getInt16(i * 2, isLittleEndian) / 32768
    }
    return float32
  }

  const appendAudioData = (newData: Float32Array): void => {
    const needed = bufLen + newData.length
    if (needed > float32Buf.length) {
      // 指数增长：翻倍直到够用
      let newSize = float32Buf.length * 2
      while (newSize < needed) newSize *= 2
      const newBuf = new Float32Array(newSize)
      newBuf.set(float32Buf.subarray(0, bufLen), 0)
      float32Buf = newBuf
    }
    float32Buf.set(newData, bufLen)
    bufLen += newData.length
  }

  const refillPcmQueue = (): void => {
    const targetSamples = TARGET_BUFFER_SAMPLES
    while (bufLen >= targetSamples) {
      const chunk = new Float32Array(targetSamples)
      chunk.set(float32Buf.subarray(0, targetSamples), 0)
      pcmQueue.value.push(chunk)
      const remaining = bufLen - targetSamples
      float32Buf.copyWithin(0, targetSamples, bufLen)
      bufLen = remaining
    }
  }

  /**
   * 播放 TTS
   * @param request TTS 请求参数
   */
  const play = async (request: TtsRequest): Promise<void> => {
    stop()
    initAudio()
    const ctx = audioContext.value
    if (ctx && ctx.state === 'suspended') {
      await ctx.resume()
    }
    isPlaying.value = true

    // 构建请求体
    const body: Record<string, unknown> = {
      text: request.text,
      voiceId: request.voiceId,
      speed: request.speed ?? 1.0,
      emotion: request.emotion ?? 'happy',
      format: request.format ?? 'pcm',
      vol: request.vol ?? 1,
      pitch: request.pitch ?? 0,
      channel: request.channel ?? 1,
      forceCbr: request.forceCbr ?? false,
      textNormalization: request.textNormalization ?? false,
      latexRead: request.latexRead ?? false,
      voicePitch: request.voicePitch ?? 0,
      voiceIntensity: request.voiceIntensity ?? 0,
      voiceTimbre: request.voiceTimbre ?? 0,
      soundEffects: request.soundEffects ?? '',
      subtitleEnable: request.subtitleEnable ?? false,
      aigcWatermark: request.aigcWatermark ?? false
    }

    currentController.value = createSSERawStream('/api/tts/speech', {
      body,
      onMessage: (msg) => {
        let response: { step?: number; data?: string }
        try {
          response = typeof msg.data === 'string' ? JSON.parse(msg.data) : msg.data
        } catch {
          return
        }
        if (!response || typeof response !== 'object') return
        if (response.step === STEP_DATA && response.data) {
          const float32 = hexToFloat32(response.data, true)
          if (!float32 || float32.length === 0) return
          appendAudioData(float32)
          refillPcmQueue()
        }
      },
      onClose: () => {
        streamEnded = true
        bufLen = 0
      },
      onError: () => {
        stop()
      }
    })
  }

  const pause = (): void => {
    if (audioContext.value && audioContext.value.state === 'running') {
      audioContext.value.suspend()
    }
    isPlaying.value = false
  }

  const resume = (): void => {
    if (audioContext.value && audioContext.value.state === 'suspended') {
      audioContext.value.resume()
    }
    isPlaying.value = true
  }

  onUnmounted(() => {
    stop()
    if (processor.value) {
      processor.value.disconnect()
      processor.value = null
    }
    if (audioContext.value) {
      audioContext.value.close()
      audioContext.value = null
    }
  })

  return {
    isPlaying,
    play,
    stop,
    pause,
    resume
  }
}
