<script setup lang="ts">
import { ref, computed } from 'vue'
import { generateImage } from '@/utils/imageApi'
import type { ImageGenerationRequest } from '@/types/image'

const props = defineProps<{
  modelValue?: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [images: string[]]
  'generate': []
}>()

const prompt = ref('')
const selectedMode = ref<'text-to-image' | 'image-to-image'>('text-to-image')
const referenceImage = ref<string | null>(null)
const referenceIntensity = ref(65)
const promptOptimizer = ref(false)
const aigcWatermark = ref(false)

// Model selection
const models = [
  { id: 'image-01', label: 'image-01', selected: true },
  { id: 'image-01-live', label: 'image-01-live', selected: false }
]
const selectedModel = ref('image-01')

// Dimensions
const aspectRatio = ref('1:1')
const imageCount = ref(1)

const decrementCount = (): void => {
  if (imageCount.value > 1) imageCount.value--
}

const incrementCount = (): void => {
  if (imageCount.value < 4) imageCount.value++
}

// Seed
const seed = ref('')
const useSeed = ref(false)

const isGenerating = ref(false)
const errorMessage = ref('')

const canGenerate = computed(() => prompt.value.trim().length > 0 && !isGenerating.value)

const handleGenerate = async (): Promise<void> => {
  if (!canGenerate.value) return

  isGenerating.value = true
  errorMessage.value = ''

  try {
    const request: ImageGenerationRequest = {
      prompt: prompt.value,
      model: selectedModel.value,
      aspectRatio: aspectRatio.value,
      n: imageCount.value,
      responseFormat: 'url',
      promptOptimizer: promptOptimizer.value,
      aigcWatermark: aigcWatermark.value
    }

    const response = await generateImage(request)

    if (response.statusCode === 0) {
      emit('update:modelValue', response.imageUrls || [])
      emit('generate')
    } else {
      errorMessage.value = response.statusMsg || 'Generation failed'
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unknown error'
  } finally {
    isGenerating.value = false
  }
}

const handleReset = (): void => {
  prompt.value = ''
  selectedModel.value = 'image-01'
  aspectRatio.value = '1:1'
  seed.value = ''
  useSeed.value = false
  referenceImage.value = null
  referenceIntensity.value = 65
  promptOptimizer.value = false
  aigcWatermark.value = false
  emit('update:modelValue', [])
  errorMessage.value = ''
}

const handleModelSelect = (modelId: string): void => {
  selectedModel.value = modelId
}

const handleRandomSeed = (): void => {
  seed.value = String(Math.floor(Math.random() * 2147483647))
}

const fillForm = (item: { description: string; model: string; aspectRatio: string; style?: string }) => {
  prompt.value = item.description
  selectedModel.value = item.model
  aspectRatio.value = item.aspectRatio
}

defineExpose({ fillForm })
</script>

<template>
  <aside class="flex flex-col bg-[#f7f9fb] h-full border-r border-[#c2c6d403]">
    <!-- Left Column: Unified Generation Controls -->
    <div class="flex-1 p-6 flex flex-col gap-6 overflow-auto">
      <!-- Header Section -->
      <div class="flex items-center gap-2">
        <div class="w-1.5 h-6 rounded-full bg-gradient-to-b from-[#003f87] to-[#0056b3]"></div>
        <h2 class="text-lg font-bold text-[#003f87]">创作实验室</h2>
      </div>

      <!-- Unified Input Container -->
      <div class="bg-white rounded-lg shadow-[0px_1px_1.75px_0px_rgba(0,0,0,0.05)] p-5 flex flex-col gap-5">
        <!-- Tab Switcher -->
        <div class="flex gap-1 p-1 bg-[#f2f4f6] rounded">
          <button
            class="flex-1 py-2.5 px-4 rounded-md text-sm font-medium transition-all"
            :class="selectedMode === 'text-to-image'
              ? 'bg-white text-[#003f87] shadow-[0px_1px_1.75px_0px_rgba(0,0,0,0.05)]'
              : 'text-[#424752]'"
            @click="selectedMode = 'text-to-image'"
          >
            文生图 (Text-to-Image)
          </button>
          <button
            class="flex-1 py-2.5 px-4 rounded-md text-sm font-medium transition-all cursor-not-allowed opacity-50"
            disabled
          >
            图生图 (Image-to-Image) 暂未开放
          </button>
        </div>

        <!-- Prompt -->
        <div class="flex flex-col gap-2">
          <label class="text-xs font-bold text-[#424752] tracking-wide uppercase">
            提示词 (Prompt / Instructions)
          </label>
          <textarea
            v-model="prompt"
            rows="5"
            maxlength="1500"
            placeholder="描述您想要生成的学术图像或输入修改指令..."
            class="w-full px-4 py-3 bg-[#f2f4f6] rounded border border-transparent focus:border-[#003f87] focus:outline-none text-sm text-[#424752] placeholder-[#c2c6d4] resize-none"
          />
        </div>

        <!-- Reference Image (Image-to-Image mode) -->
        <div v-if="selectedMode === 'image-to-image'" class="flex flex-col gap-4">
          <label class="text-xs font-bold text-[#424752] tracking-wide uppercase">
            参考图像 (Reference Image)
          </label>
          <div class="border-2 border-dashed border-[#c2c6d4] rounded p-8 flex flex-col items-center justify-center gap-2 bg-[#f2f4f6]">
            <SvgIcon name="image-upload" class="w-5 h-6.25 text-[#c2c6d4]" />
            <span class="text-xs font-medium text-[#424752]">点击或拖拽参考图至此处</span>
          </div>

          <!-- Reference Intensity Slider -->
          <!-- <div class="flex items-center justify-between">
            <span class="text-xs font-medium text-[#424752]">参考强度</span>
            <span class="text-sm font-bold text-[#003f87]">{{ referenceIntensity }}%</span>
          </div>
          <div class="h-1.5 bg-[#e6e8ea] rounded"></div> -->
        </div>

        <!-- Model Selection -->
        <div class="flex flex-col gap-2">
          <label class="text-xs font-bold text-[#424752] tracking-wide uppercase">模型选择</label>
          <div class="flex gap-2">
            <button
              v-for="model in models"
              :key="model.id"
              class="px-3 py-1.5 rounded-xl text-xs font-medium transition-all"
              :class="selectedModel === model.id
                ? 'bg-[#003f8701] text-[#003f87] border border-[#003f87]'
                : 'text-[#424752] border border-[#c2c6d4]'"
              @click="handleModelSelect(model.id)"
            >
              {{ model.label }}
            </button>
          </div>
        </div>

        <!-- Dimensions -->
        <div class="flex flex-col gap-2">
          <div class="flex justify-between text-xs font-bold text-[#424752] tracking-wide uppercase">
            <span>图片尺寸</span>
            <span>生成数量</span>
          </div>
          <div class="flex gap-3">
            <select
              v-model="aspectRatio"
              class="flex-1 px-3 py-2 bg-[#f2f4f6] rounded text-sm text-[#424752] border border-transparent focus:border-[#003f87] focus:outline-none appearance-none cursor-pointer"
            >
              <option value="1:1">1:1 (正方形)</option>
              <option value="16:9">16:9 (宽屏)</option>
              <option value="4:3">4:3 (标准)</option>
              <option value="3:2">3:2 (照片)</option>
              <option value="2:3">2:3 (竖版)</option>
              <option value="3:4">3:4 (竖版)</option>
              <option value="9:16">9:16 (手机)</option>
              <option value="21:9">21:9 (超宽)</option>
            </select>
            <div class="flex items-center gap-1">
              <button
                class="w-8 h-8 rounded bg-[#f2f4f6] text-[#424752] hover:bg-[#e6e8ea] transition-colors flex items-center justify-center"
                @click="decrementCount"
              >
                <span class="text-sm font-bold">−</span>
              </button>
              <span class="w-8 text-center text-sm font-bold text-[#003f87]">{{ imageCount }}</span>
              <button
                class="w-8 h-8 rounded bg-[#f2f4f6] text-[#424752] hover:bg-[#e6e8ea] transition-colors flex items-center justify-center"
                @click="incrementCount"
              >
                <span class="text-sm font-bold">+</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Advanced Settings -->
        <el-collapse class="!border-none" style="--el-collapse-header-height: 36px;">
          <el-collapse-item name="advanced">
            <template #title>
              <div class="flex items-center gap-2 pl-2 py-1">
                <SvgIcon name="settings" class="w-3.5 h-3.5 text-[#003f87]" />
                <span class="text-xs font-bold text-[#424752] tracking-wide">高级设置</span>
              </div>
            </template>
            <!-- Advanced Settings Content -->
            <div class="bg-[#f2f4f6] rounded-lg p-4 space-y-4">
              <!-- Seed Input -->
              <div class="space-y-2">
                <label class="text-[11px] font-bold text-[#424752] tracking-wide uppercase">随机种子 (Seed)</label>
                <div class="flex gap-2">
                  <el-input
                    v-model="seed"
                    type="text"
                    placeholder="输入种子值或留空随机"
                    clearable
                    size="default"
                    class="flex-1"
                  />
                  <el-button
                    size="default"
                    class="!px-3"
                    @click="handleRandomSeed"
                  >
                    <SvgIcon name="refresh" class="w-4 h-4" />
                  </el-button>
                </div>
              </div>

              <!-- Divider -->
              <div class="border-t border-[#c2c6d4]/40"></div>

              <!-- Toggle Rows -->
              <div class="space-y-3">
                <!-- Prompt Optimizer -->
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-[#003f87]/10 to-[#0056b3]/10 flex items-center justify-center">
                      <SvgIcon name="star" class="w-5 h-5 text-[#003f87]" />
                    </div>
                    <div>
                      <span class="text-sm font-medium text-[#424752]">Prompt 优化</span>
                      <p class="text-[11px] text-[#8f96a1]">自动优化描述词以获得更好的效果</p>
                    </div>
                  </div>
                  <el-switch v-model="promptOptimizer" />
                </div>

                <!-- AI Watermark -->
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-[#003f87]/10 to-[#0056b3]/10 flex items-center justify-center">
                      <SvgIcon name="tag" class="w-5 h-5 text-[#003f87]" />
                    </div>
                    <div>
                      <span class="text-sm font-medium text-[#424752]">AI 水印</span>
                      <p class="text-[11px] text-[#8f96a1]">为生成的图像添加 AI 内容标识</p>
                    </div>
                  </div>
                  <el-switch v-model="aigcWatermark" />
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>

        <!-- Action Buttons -->
        <div class="flex gap-3 pt-2 justify-center">
          <button
            class="px-6 py-3 bg-[#e6e8ea] text-[#191c1e] font-bold text-sm rounded"
            @click="handleReset"
          >
            重置
          </button>
          <button
            :disabled="!canGenerate"
            class="relative px-8 py-3 bg-gradient-to-r from-[#003f87] to-[#0056b3] text-white font-bold text-sm rounded shadow-[0px_2px_3.5px_-2px_rgba(0,0,0,0.1),0px_4px_5.25px_-1px_rgba(0,0,0,0.1)] disabled:opacity-50 disabled:cursor-not-allowed"
            @click="handleGenerate"
          >
            <span v-if="isGenerating">生成中...</span>
            <span v-else class="flex items-center gap-2">
              <SvgIcon name="magic-wand" class="w-4 h-4" />
              开始生成
            </span>
          </button>
        </div>

        <!-- Error Message -->
        <div
          v-if="errorMessage"
          class="p-3 bg-red-50 text-red-600 text-sm rounded-lg"
        >
          {{ errorMessage }}
        </div>
      </div>
    </div>
  </aside>
</template>
