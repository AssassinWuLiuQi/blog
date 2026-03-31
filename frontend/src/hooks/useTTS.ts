import { ref, onUnmounted } from 'vue'
import { createSSERawStream } from '@/utils/sse'
import type { TTSOptions } from '@/types'

const STEP_INIT = 0
const STEP_DATA = 100
const STEP_ERROR = 400
const STEP_COMPLETE = 1000

export function useTTS(options: TTSOptions = {}) {
  const {
    sampleRate = 24000,
    bufferSize = 4096,
    channels = 1
  } = options

  const audioContext = ref<AudioContext | null>(null)
  const processor = ref<ScriptProcessorNode | null>(null)
  const pcmQueue = ref<Float32Array[]>([])
  const isPlaying = ref(false)
  const isStreamEnded = ref(false)
  const currentController = ref<AbortController | null>(null)

  // 初始化 AudioContext
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

  // 停止播放
  const stop = (): void => {
    if (currentController.value) {
      currentController.value.abort()
      currentController.value = null
    }

    pcmQueue.value = []
    isPlaying.value = false
    isStreamEnded.value = false
  }

  // 将十六进制字符串转换为 Float32Array (PCM 16bit -> Float32)
  const hexToFloat32 = (hexString: string, isLittleEndian: boolean = false): Float32Array => {
    const bytes = new Uint8Array(hexString.length / 2)
    for (let i = 0; i < hexString.length; i += 2) {
      bytes[i / 2] = parseInt(hexString.substr(i, 2), 16)
    }

    const int16 = new Int16Array(bytes.buffer)
    const float32 = new Float32Array(int16.length)

    if (isLittleEndian) {
      // 小端序
      for (let i = 0; i < int16.length; i++) {
        float32[i] = int16[i] / 32768
      }
    } else {
      // 大端序 - 交换字节
      for (let i = 0; i < int16.length; i++) {
        const b0 = bytes[i * 2]
        const b1 = bytes[i * 2 + 1]
        int16[i] = (b0 << 8) | b1
        float32[i] = int16[i] / 32768
      }
    }

    return float32
  }

  // 播放
  const play = async (text: string, voiceId: string): Promise<void> => {
    console.log('TTS play called', { text, voiceId })

    // 停止之前的播放
    stop()

    // 初始化音频上下文
    initAudio()

    // 确保 AudioContext 处于运行状态
    const ctx = audioContext.value
    if (ctx && ctx.state === 'suspended') {
      await ctx.resume()
    }

    isPlaying.value = true

    console.log('Creating SSE stream...')

    // SSE 流式请求
    currentController.value = createSSERawStream('/api/tts/speech', {
      body: { text, voiceId },
      onOpen: () => {
        console.log('TTS onOpen: 连接成功')
      },
      onMessage: (msg) => {
        const response = typeof msg.data === 'string' ? JSON.parse(msg.data) : msg.data
        console.log('TTS onMessage:', response)
        if (!response || typeof response !== 'object') return

        switch (response.step) {
          case STEP_INIT:
            console.log('TTS init:', response.message)
            break
          case STEP_DATA:
            if (response.data) {
              const float32 = hexToFloat32(response.data, false) // 大端序
              console.log('PCM samples:', float32.slice(0, 10)) // 打印前10个样本看看
              pcmQueue.value.push(float32)
            }
            break
          case STEP_ERROR:
            console.error('TTS error:', response.message)
            break
          case STEP_COMPLETE:
            console.log('TTS complete')
            isStreamEnded.value = true
            break
        }
      },
      onClose: () => {
        console.log('TTS onClose')
        isStreamEnded.value = true
        // 流关闭后，等待队列播放完再清理
        waitForQueueEmpty()
      },
      onError: (err: Error) => {
        console.error('TTS stream error:', err)
        isStreamEnded.value = true
        stop()
      }
    })

    console.log('SSE stream created')
  }

  // 等待队列播放完再清理
  const waitForQueueEmpty = (): void => {
    const check = (): void => {
      if (pcmQueue.value.length === 0) {
        // 队列播完了，清理资源
        stop()
      } else {
        // 还有数据，100ms后再检查
        setTimeout(check, 100)
      }
    }
    setTimeout(check, 100)
  }

  // 暂停/恢复
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

  // 清理
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
