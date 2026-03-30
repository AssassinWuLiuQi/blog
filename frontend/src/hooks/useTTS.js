import { ref, onUnmounted } from 'vue'
import { createSSERawStream } from '@/utils/sse'

export function useTTS(options = {}) {
  const {
    sampleRate = 24000,
    bufferSize = 4096,
    channels = 1
  } = options

  const audioContext = ref(null)
  const processor = ref(null)
  const pcmQueue = ref([])
  const isPlaying = ref(false)
  const currentController = ref(null)

  // 初始化 AudioContext
  const initAudio = () => {
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
  const stop = () => {
    if (currentController.value) {
      currentController.value.abort()
      currentController.value = null
    }

    pcmQueue.value = []
    isPlaying.value = false
  }

  // 播放
  const play = async (text, voiceId) => {
    // 停止之前的播放
    stop()

    // 初始化音频上下文
    initAudio()

    // 确保 AudioContext 处于运行状态
    if (audioContext.value.state === 'suspended') {
      await audioContext.value.resume()
    }

    isPlaying.value = true

    // SSE 流式请求
    currentController.value = createSSERawStream('/api/tts/speech', {
      body: { text, voiceId },
      onMessage: (msg) => {
        if (msg.data) {
          // msg.data 是原始 PCM 字节 (Uint8Array)
          const uint8 = new Uint8Array(msg.data)
          const int16 = new Int16Array(uint8.buffer)

          // 转换为 Float32 (-1 到 1)
          const float32 = new Float32Array(int16.length)
          for (let i = 0; i < int16.length; i++) {
            float32[i] = int16[i] / 32768
          }

          pcmQueue.value.push(float32)
        }
      },
      onClose: () => {
        isPlaying.value = false
      },
      onError: (err) => {
        console.error('TTS stream error:', err)
        isPlaying.value = false
      }
    })
  }

  // 暂停/恢复
  const pause = () => {
    if (audioContext.value && audioContext.value.state === 'running') {
      audioContext.value.suspend()
    }
    isPlaying.value = false
  }

  const resume = () => {
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
