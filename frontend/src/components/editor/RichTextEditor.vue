<script setup lang="ts">
import { ref, onBeforeUnmount, watch, onMounted } from 'vue'
import { useEditor, EditorContent, type Editor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import { TextStyle } from '@tiptap/extension-text-style'
import Color from '@tiptap/extension-color'
import { TextAlign } from '@tiptap/extension-text-align'
import GradientButton from '@/components/common/GradientButton.vue'

interface Props {
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: ''
})

const emit = defineEmits<{
  'export': [html: string | undefined]
  'update:modelValue': [text: string]
}>()

interface ColorOption {
  name: string
  value: string
}

interface FontSizeOption {
  name: string
  value: string
}

const editor = useEditor({
  extensions: [
    StarterKit,
    TextStyle,
    Color,
    TextAlign.configure({
      types: ['heading', 'paragraph']
    }),
    Placeholder.configure({
      placeholder: '开始输入内容...'
    })
  ],
  content: props.modelValue || `
    <p>梨花落了春红，太匆匆</p>
  `,
  editorProps: {
    attributes: {
      class: 'prose prose-lg max-w-none focus:outline-none chinese-manuscript'
    }
  },
  onUpdate: ({ editor }: { editor: Editor }) => {
    emit('update:modelValue', editor.getText())
  }
})

// 监听外部 content 变化
watch(() => props.modelValue, (newVal: string) => {
  if (editor.value && newVal !== editor.value.getText()) {
    editor.value.commands.setContent(newVal)
  }
})

// 挂载时也触发一次更新，传递默认内容
onMounted(() => {
  if (editor.value) {
    emit('update:modelValue', editor.value.getText())
  }
})

const autoSaveTime = ref('14:20')
const headingDropdownOpen = ref(false)
const colorDropdownOpen = ref(false)

const colors: ColorOption[] = [
  { name: '默认', value: '#191c1e' },
  { name: '蓝色', value: '#003f87' },
  { name: '深蓝', value: '#002b59' },
  { name: '青色', value: '#006874' },
  { name: '绿色', value: '#006e1c' },
  { name: '橙色', value: '#8b3900' },
  { name: '红色', value: '#ba1a1a' },
]

const fontSizes: FontSizeOption[] = [
  { name: '小', value: '14px' },
  { name: '正常', value: '16px' },
  { name: '中', value: '18px' },
  { name: '大', value: '20px' },
  { name: '特大', value: '24px' },
]

const toggleBold = (): void => { editor.value?.chain().focus().toggleBold().run() }
const toggleItalic = (): void => { editor.value?.chain().focus().toggleItalic().run() }
const toggleStrike = (): void => { editor.value?.chain().focus().toggleStrike().run() }
const toggleBulletList = (): void => { editor.value?.chain().focus().toggleBulletList().run() }
const toggleOrderedList = (): void => { editor.value?.chain().focus().toggleOrderedList().run() }
const toggleCodeBlock = (): void => { editor.value?.chain().focus().toggleCodeBlock().run() }
const toggleBlockquote = (): void => { editor.value?.chain().focus().toggleBlockquote().run() }
const toggleHeading = (level: number): void => {
  editor.value?.chain().focus().toggleHeading({ level }).run()
  headingDropdownOpen.value = false
}
const setColor = (color: string): void => {
  editor.value?.chain().focus().setColor(color).run()
  colorDropdownOpen.value = false
}
const setFontSize = (size: string): void => {
  editor.value?.chain().focus().setMarkAttribute('textStyle', 'fontSize', size).run()
}

const isActive = (type: string): boolean | undefined => editor.value?.isActive(type)

const closeHeadingDropdown = (): void => {
  headingDropdownOpen.value = false
}
const closeColorDropdown = (): void => {
  colorDropdownOpen.value = false
}

const handleExport = (): void => {
  emit('export', editor.value?.getHTML())
}

// 获取纯文本内容
const getTextContent = (): string => {
  return editor.value?.getText() || ''
}

defineExpose({
  getTextContent
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<template>
  <div class="flex flex-col bg-surface-container-lowest rounded-xl shadow-sm border border-outline-variant/10 overflow-hidden h-full">
    <!-- Toolbar -->
    <div class="px-6 py-3 bg-surface-container-low flex items-center justify-between border-b border-outline-variant/20">
      <!-- Left: Format & List Buttons -->
      <div class="flex items-center gap-1">
        <!-- Bold Button -->
        <button
          @click="toggleBold"
          class="p-2 hover:bg-white rounded transition-colors"
          :class="isActive('bold') ? 'text-primary' : 'text-on-surface-variant'"
          title="粗体"
        >
          <span class="material-symbols-outlined text-lg">format_bold</span>
        </button>

        <!-- Italic Button -->
        <button
          @click="toggleItalic"
          class="p-2 hover:bg-white rounded transition-colors"
          :class="isActive('italic') ? 'text-primary' : 'text-on-surface-variant'"
          title="斜体"
        >
          <span class="material-symbols-outlined text-lg">format_italic</span>
        </button>

        <!-- Strikethrough Button -->
        <button
          @click="toggleStrike"
          class="p-2 hover:bg-white rounded transition-colors"
          :class="isActive('strike') ? 'text-primary' : 'text-on-surface-variant'"
          title="删除线"
        >
          <span class="material-symbols-outlined text-lg">strikethrough_s</span>
        </button>

        <!-- Divider -->
        <div class="w-px h-6 bg-outline-variant/30 mx-2"></div>

        <!-- Heading Dropdown -->
        <div class="relative">
          <button
            @click="headingDropdownOpen = !headingDropdownOpen"
            class="p-2 hover:bg-white rounded transition-colors flex items-center gap-1"
            :class="isActive('heading') ? 'text-primary' : 'text-on-surface-variant'"
            title="标题"
          >
            <span class="material-symbols-outlined text-lg">title</span>
            <span class="material-symbols-outlined text-sm">expand_more</span>
          </button>
          <!-- Dropdown Menu -->
          <div
            v-show="headingDropdownOpen"
            class="absolute top-full left-0 mt-1 py-2 bg-white rounded-lg shadow-lg border border-outline-variant z-50 min-w-[120px]"
          >
            <button
              v-for="level in [1, 2, 3, 4, 5, 6]"
              :key="level"
              @click="toggleHeading(level)"
              class="w-full px-4 py-2 text-left hover:bg-surface-container-low transition-colors flex items-center gap-2"
              :class="isActive('heading', { level }) ? 'text-primary font-medium' : 'text-on-surface-variant'"
            >
              <span class="text-sm font-medium">标题 {{ level }}</span>
            </button>
          </div>
        </div>

        <!-- Code Block Button -->
        <button
          @click="toggleCodeBlock"
          class="p-2 hover:bg-white rounded transition-colors"
          :class="isActive('codeBlock') ? 'text-primary' : 'text-on-surface-variant'"
          title="代码块"
        >
          <span class="material-symbols-outlined text-lg">data_object</span>
        </button>

        <!-- Blockquote Button -->
        <button
          @click="toggleBlockquote"
          class="p-2 hover:bg-white rounded transition-colors"
          :class="isActive('blockquote') ? 'text-primary' : 'text-on-surface-variant'"
          title="引用"
        >
          <span class="material-symbols-outlined text-lg">format_quote</span>
        </button>

        <!-- Divider -->
        <div class="w-px h-6 bg-outline-variant/30 mx-2"></div>

        <!-- List Buttons -->
        <button
          @click="toggleBulletList"
          class="p-2 hover:bg-white rounded transition-colors"
          :class="isActive('bulletList') ? 'text-primary' : 'text-on-surface-variant'"
          title="无序列表"
        >
          <span class="material-symbols-outlined text-lg">format_list_bulleted</span>
        </button>
        <button
          @click="toggleOrderedList"
          class="p-2 hover:bg-white rounded transition-colors"
          :class="isActive('orderedList') ? 'text-primary' : 'text-on-surface-variant'"
          title="有序列表"
        >
          <span class="material-symbols-outlined text-lg">format_list_numbered</span>
        </button>

        <!-- Divider -->
        <div class="w-px h-6 bg-outline-variant/30 mx-2"></div>

        <!-- Color Dropdown -->
        <div class="relative">
          <button
            @click="colorDropdownOpen = !colorDropdownOpen"
            class="p-2 hover:bg-white rounded transition-colors text-on-surface-variant flex items-center gap-1"
            title="字体颜色"
          >
            <span class="material-symbols-outlined text-lg">palette</span>
            <span class="material-symbols-outlined text-sm">expand_more</span>
          </button>
          <!-- Color Dropdown Menu -->
          <div
            v-show="colorDropdownOpen"
            class="absolute top-full left-0 mt-1 py-3 px-4 bg-white rounded-lg shadow-lg border border-outline-variant z-50 min-w-[180px]"
          >
            <div class="grid grid-cols-4 gap-2 mb-3">
              <button
                v-for="color in colors"
                :key="color.value"
                @click="setColor(color.value)"
                class="w-8 h-8 rounded-full border border-outline-variant hover:scale-110 transition-transform"
                :style="{ backgroundColor: color.value }"
                :title="color.name"
              ></button>
            </div>
            <div class="border-t border-outline-variant pt-3 mt-2">
              <p class="text-xs text-on-surface-variant mb-2">字号</p>
              <div class="flex flex-wrap gap-1">
                <button
                  v-for="size in fontSizes"
                  :key="size.value"
                  @click="setFontSize(size.value)"
                  class="px-2 py-1 text-xs rounded hover:bg-surface-container-low transition-colors"
                  :class="isActive('textStyle', { fontSize: size.value }) ? 'bg-primary text-white' : 'text-on-surface-variant'"
                >
                  {{ size.name }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Auto-save Status + Export Button -->
      <div class="flex items-center gap-3">
        <span class="text-xs text-on-surface-variant/60 italic">自动保存于 {{ autoSaveTime }}</span>
        <GradientButton label="导出稿件" @click="emit('export', editor?.getHTML())" />
      </div>
    </div>

    <!-- Editor Content -->
    <div class="flex-1 p-12 overflow-y-auto" @click="closeHeadingDropdown(); closeColorDropdown()">
      <EditorContent :editor="editor" />
    </div>
  </div>
</template>

<style>
.chinese-manuscript {
  line-height: 1.8;
  letter-spacing: 0.02em;
}

.chinese-manuscript h1 {
  @apply text-4xl font-bold text-blue-900 mb-6 tracking-tight;
}

.chinese-manuscript h2 {
  @apply text-3xl font-semibold text-blue-900 mb-6 tracking-tight;
}

.chinese-manuscript h3 {
  @apply text-2xl font-semibold text-blue-900 mb-4;
}

.chinese-manuscript h4 {
  @apply text-xl font-semibold text-on-surface mb-3;
}

.chinese-manuscript h5 {
  @apply text-lg font-medium text-on-surface mb-2;
}

.chinese-manuscript h6 {
  @apply text-base font-medium text-on-surface-variant mb-2;
}

.chinese-manuscript p {
  @apply mb-6 text-on-surface;
}

.chinese-manuscript blockquote {
  @apply my-8 pl-6 border-l-4 italic text-on-surface-variant py-4 pr-4 rounded-r-lg;
  border-left-color: rgba(0, 63, 135, 0.2);
  background-color: rgba(236, 238, 240, 0.5);
}

.chinese-manuscript pre {
  @apply my-6 p-4 bg-surface-container rounded-lg overflow-x-auto;
}

.chinese-manuscript code {
  @apply text-sm font-mono text-primary bg-surface-container-low px-1 py-0.5 rounded;
}

.chinese-manuscript pre code {
  @apply bg-transparent p-0 text-on-surface;
}

.chinese-manuscript ul {
  @apply list-disc pl-6 mb-6;
}

.chinese-manuscript ol {
  @apply list-decimal pl-6 mb-6;
}

.chinese-manuscript li {
  @apply mb-2;
}

/* Placeholder */
.tiptap p.is-editor-empty:first-child::before {
  color: #424752;
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}
</style>
