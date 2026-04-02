<script setup lang="ts">
import { ref, computed } from 'vue'
import { generateImage } from '@/utils/imageApi'
import type { ImageGenerationRequest } from '@/types/image'

const prompt = ref('')
const selectedModel = ref<'image-01' | 'image-01-live'>('image-01')
const selectedAspectRatio = ref('1:1')
const imageCount = ref(1)
const promptOptimizer = ref(false)
const aigcWatermark = ref(false)

const isGenerating = ref(false)
const generatedImages = ref<string[]>([])
const errorMessage = ref('')

const aspectRatioOptions = [
  { value: '1:1', label: '1:1 (Square)' },
  { value: '16:9', label: '16:9 (Landscape)' },
  { value: '9:16', label: '9:16 (Portrait)' },
  { value: '4:3', label: '4:3 (Standard)' },
  { value: '3:2', label: '3:2 (Photo)' }
]

const canGenerate = computed(() => prompt.value.trim().length > 0 && !isGenerating.value)

const handleGenerate = async (): Promise<void> => {
  if (!canGenerate.value) return

  isGenerating.value = true
  errorMessage.value = ''
  generatedImages.value = []

  try {
    const request: ImageGenerationRequest = {
      prompt: prompt.value,
      model: selectedModel.value,
      aspectRatio: selectedAspectRatio.value,
      n: imageCount.value,
      responseFormat: 'url',
      promptOptimizer: promptOptimizer.value,
      aigcWatermark: aigcWatermark.value
    }

    const response = await generateImage(request)

    if (response.statusCode === 0) {
      generatedImages.value = response.imageUrls || []
    } else {
      errorMessage.value = response.statusMsg || 'Generation failed'
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unknown error'
  } finally {
    isGenerating.value = false
  }
}

const handleDownload = (url: string, index: number): void => {
  const link = document.createElement('a')
  link.href = url
  link.download = `generated-image-${index + 1}.png`
  link.target = '_blank'
  link.click()
}
</script>

<template>
  <aside class="flex-1 min-w-[320px] flex flex-col gap-6">
    <div class="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm">
      <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">
        AI Image Generation
      </h2>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Prompt
        </label>
        <textarea
          v-model="prompt"
          rows="4"
          maxlength="1500"
          placeholder="Describe the image you want to generate..."
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg
                 focus:ring-2 focus:ring-blue-500 focus:border-transparent
                 bg-white dark:bg-gray-700 text-gray-900 dark:text-white
                 placeholder-gray-400 dark:placeholder-gray-500 resize-none"
        />
        <div class="text-xs text-gray-500 dark:text-gray-400 mt-1 text-right">
          {{ prompt.length }}/1500
        </div>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Model
        </label>
        <div class="flex gap-4">
          <label class="flex items-center cursor-pointer">
            <input
              v-model="selectedModel"
              type="radio"
              value="image-01"
              class="w-4 h-4 text-blue-600 focus:ring-blue-500"
            />
            <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">image-01</span>
          </label>
          <label class="flex items-center cursor-pointer">
            <input
              v-model="selectedModel"
              type="radio"
              value="image-01-live"
              class="w-4 h-4 text-blue-600 focus:ring-blue-500"
            />
            <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">image-01-live</span>
          </label>
        </div>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Aspect Ratio
        </label>
        <select
          v-model="selectedAspectRatio"
          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg
                 focus:ring-2 focus:ring-blue-500 focus:border-transparent
                 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
        >
          <option v-for="opt in aspectRatioOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Number of Images: {{ imageCount }}
        </label>
        <input
          v-model.number="imageCount"
          type="range"
          min="1"
          max="9"
          class="w-full h-2 bg-gray-200 dark:bg-gray-600 rounded-lg appearance-none cursor-pointer"
        />
      </div>

      <div class="mb-4 space-y-2">
        <label class="flex items-center cursor-pointer">
          <input
            v-model="promptOptimizer"
            type="checkbox"
            class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
          />
          <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">
            Prompt Optimizer
          </span>
        </label>
        <label class="flex items-center cursor-pointer">
          <input
            v-model="aigcWatermark"
            type="checkbox"
            class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
          />
          <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">
            AIGC Watermark
          </span>
        </label>
      </div>

      <button
        :disabled="!canGenerate"
        class="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400
               text-white font-medium rounded-lg transition-colors
               disabled:cursor-not-allowed"
        @click="handleGenerate"
      >
        {{ isGenerating ? 'Generating...' : 'Generate Image' }}
      </button>

      <div
        v-if="errorMessage"
        class="mt-4 p-3 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400
               text-sm rounded-lg"
      >
        {{ errorMessage }}
      </div>
    </div>

    <div
      v-if="generatedImages.length > 0"
      class="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm"
    >
      <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">
        Generated Images
      </h3>
      <div class="grid gap-4" :class="generatedImages.length === 1 ? 'grid-cols-1' : 'grid-cols-2'">
        <div
          v-for="(url, index) in generatedImages"
          :key="index"
          class="relative group"
        >
          <img
            :src="url"
            :alt="`Generated image ${index + 1}`"
            class="w-full rounded-lg"
          />
          <div
            class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100
                   transition-opacity flex items-center justify-center rounded-lg"
          >
            <button
              class="px-4 py-2 bg-white text-gray-900 font-medium rounded-lg
                     hover:bg-gray-100 transition-colors"
              @click="handleDownload(url, index)"
            >
              Download
            </button>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>
