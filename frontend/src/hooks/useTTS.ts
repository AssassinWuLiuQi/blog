import { ref, onUnmounted } from 'vue'
import { createSSERawStream } from '@/utils/sse'
import type { TtsRequest } from '@/types/tts'

const STEP_DATA = 100
const STEP_COMPLETE = 1000

// 目标缓冲采样点数
const TARGET_BUFFER_SAMPLES = 8192

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
  const isStreamEnded = ref(false)
  const currentController = ref<AbortController | null>(null)

  // 累积 Float32 数据的缓冲区
  const float32Buffer = ref<Float32Array>(new Float32Array(0))
  const targetSamples = TARGET_BUFFER_SAMPLES

  const initAudio = (): void => {
    if (audioContext.value) return
    audioContext.value = new AudioContext({ sampleRate })
    processor.value = audioContext.value.createScriptProcessor(bufferSize, channels, channels)
    processor.value.connect(audioContext.value.destination)

    processor.value.onaudioprocess = (e) => {
      const output = e.outputBuffer.getChannelData(0)
      if (pcmQueue.value.length === 0) {
        output.fill(0)
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
    float32Buffer.value = new Float32Array(0)
    isPlaying.value = false
    isStreamEnded.value = false
  }

  /**
   * 将 hex 字符串转换为 Float32Array
   */
  const hexToFloat32 = (hexString: string, isLittleEndian: boolean = false): Float32Array | null => {
    if (hexString.length % 2 !== 0) {
      console.warn('[TTS] Odd-length hex received, dropping last char')
      hexString = hexString.slice(0, -1)
      if (hexString.length === 0) return null
    }
    const byteLength = hexString.length / 2
    const bytes = new Uint8Array(byteLength)
    for (let i = 0; i < hexString.length; i += 2) {
      bytes[i / 2] = parseInt(hexString.substring(i, i + 2), 16)
    }
    const int16Array = new Int16Array(byteLength)
    const float32 = new Float32Array(byteLength)
    if (isLittleEndian) {
      for (let i = 0; i < byteLength; i++) {
        int16Array[i] = bytes[i * 2] | (bytes[i * 2 + 1] << 8)
        float32[i] = int16Array[i] / 32768
      }
    } else {
      for (let i = 0; i < byteLength; i++) {
        const b0 = bytes[i * 2]
        const b1 = bytes[i * 2 + 1]
        int16Array[i] = (b0 << 8) | b1
        float32[i] = int16Array[i] / 32768
      }
    }
    return float32
  }

  /**
   * 合并新数据到临时缓冲区
   */
  const mergeToFloat32Buffer = (newData: Float32Array): void => {
    const combined = new Float32Array(float32Buffer.value.length + newData.length)
    combined.set(float32Buffer.value, 0)
    combined.set(newData, float32Buffer.value.length)
    float32Buffer.value = combined
  }

  /**
   * 处理临时缓冲区，提取固定长度的数据块
   */
  const processFloat32Buffer = (): void => {
    while (float32Buffer.value.length >= targetSamples) {
      const chunk = float32Buffer.value.subarray(0, targetSamples)
      pcmQueue.value.push(chunk)
      if (float32Buffer.value.length > targetSamples) {
        float32Buffer.value = float32Buffer.value.subarray(targetSamples)
      } else {
        float32Buffer.value = new Float32Array(0)
      }
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
      onOpen: () => {},
      onMessage: (msg) => {
        const response = typeof msg.data === 'string' ? JSON.parse(msg.data) : msg.data
        if (!response || typeof response !== 'object') return
        switch (response.step) {
          case STEP_DATA:
            if (response.data) {
              const float32 = hexToFloat32(response.data, true)
              if (!float32 || float32.length === 0) break
              mergeToFloat32Buffer(float32)
              processFloat32Buffer()
            }
            break
          case STEP_COMPLETE:
            isStreamEnded.value = true
            break
        }
      },
      onClose: () => {
        float32Buffer.value = new Float32Array(0)
        isStreamEnded.value = true
        waitForQueueEmpty()
      },
      onError: (_err: Error) => {
        isStreamEnded.value = true
        stop()
      }
    })
  }

  const waitForQueueEmpty = (): void => {
    const check = (): void => {
      if (pcmQueue.value.length === 0) {
        stop()
      } else {
        setTimeout(check, 100)
      }
    }
    setTimeout(check, 100)
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
