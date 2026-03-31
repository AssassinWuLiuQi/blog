import { ref, onUnmounted } from 'vue'
import { createSSERawStream } from '@/utils/sse'
import type { TTSOptions } from '@/types'

const STEP_DATA = 100
const STEP_COMPLETE = 1000

export function useTTS(options: TTSOptions = {}) {
  const {
    sampleRate = 32000,
    bufferSize = 4096,
    channels = 1
  } = options

  const audioContext = ref<AudioContext | null>(null)
  const processor = ref<ScriptProcessorNode | null>(null)
  const pcmQueue = ref<Float32Array[]>([])
  const isPlaying = ref(false)
  const isStreamEnded = ref(false)
  const currentController = ref<AbortController | null>(null)

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
    isPlaying.value = false
    isStreamEnded.value = false
  }

  const hexToFloat32 = (hexString: string, isLittleEndian: boolean = false): Float32Array => {
    const paddedHex = hexString.length % 2 === 0 ? hexString : hexString + '0'
    const byteLength = paddedHex.length / 2
    const bytes = new Uint8Array(byteLength)
    for (let i = 0; i < paddedHex.length; i += 2) {
      bytes[i / 2] = parseInt(paddedHex.substr(i, 2), 16)
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

  const play = async (text: string, voiceId: string): Promise<void> => {
    stop()
    initAudio()
    const ctx = audioContext.value
    if (ctx && ctx.state === 'suspended') {
      await ctx.resume()
    }
    isPlaying.value = true

    currentController.value = createSSERawStream('/api/tts/speech', {
      body: { text, voiceId },
      onOpen: () => {},
      onMessage: (msg) => {
        const response = typeof msg.data === 'string' ? JSON.parse(msg.data) : msg.data
        if (!response || typeof response !== 'object') return
        switch (response.step) {
          case STEP_DATA:
            if (response.data) {
              const float32 = hexToFloat32(response.data, false)
              pcmQueue.value.push(float32)
            }
            break
          case STEP_COMPLETE:
            isStreamEnded.value = true
            break
        }
      },
      onClose: () => {
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
