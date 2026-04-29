<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface PaperItem {
  id: string
  name: string
  emoji: string
  price: number
}

interface Recipient {
  id: string
  name: string
  relation: string
}

interface BurningPaper {
  x: number
  y: number
  size: number
  rotation: number
  speed: number
  item: PaperItem
  burned: boolean
  burnProgress: number
  wobble: number
}

interface Particle {
  x: number
  y: number
  size: number
  life: number
  maxLife: number
  vx: number
  vy: number
  color: string
  type: 'flame' | 'spark' | 'smoke' | 'paper'
}

const recipients = ref<Recipient[]>([
  { id: '1', name: '爷爷', relation: '祖父' },
  { id: '2', name: '奶奶', relation: '祖母' },
  { id: '3', name: '外公', relation: '外祖父' },
  { id: '4', name: '外婆', relation: '外祖母' },
  { id: '5', name: '父亲', relation: '父亲' },
  { id: '6', name: '母亲', relation: '母亲' },
  { id: '7', name: '祖先', relation: '先祖' },
  { id: '8', name: '故友', relation: '友人' },
])

const items = ref<PaperItem[]>([
  { id: 'money', name: '冥币', emoji: '💰', price: 100 },
  { id: 'gold', name: '金元宝', emoji: '🏺', price: 500 },
  { id: 'house', name: '别墅', emoji: '🏠', price: 1000 },
  { id: 'car', name: '豪车', emoji: '🚗', price: 2000 },
  { id: 'phone', name: '手机', emoji: '📱', price: 800 },
  { id: 'clothes', name: '衣服', emoji: '👔', price: 300 },
  { id: 'food', name: '美食', emoji: '🍖', price: 200 },
  { id: 'cigarette', name: '香烟', emoji: '🚬', price: 50 },
  { id: 'aitoken', name: 'AI算力', emoji: '⚡', price: 9999 },
])

const flameColors = ref([
  { id: 'classic', name: '经典火', colors: ['#ff6b00', '#ff4500', '#ff8c00', '#ffd700'] },
  { id: 'blue', name: '幽冥蓝', colors: ['#00bfff', '#1e90ff', '#4169e1', '#87ceeb'] },
  { id: 'white', name: '白焰', colors: ['#ffffff', '#fffacd', '#f0e68c', '#ffdab9'] },
])

const selectedRecipient = ref<string>('')
const selectedItems = ref<string[]>([])
const selectedFlame = ref<string>('classic')
const hasUserSelected = ref(false)

const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let animationId: number

const papers = ref<BurningPaper[]>([])
const particles = ref<Particle[]>([])

const selectedRecipientName = computed(() =>
  recipients.value.find(r => r.id === selectedRecipient.value)?.name || '请选择祭拜对象'
)

const selectedItemData = computed(() => {
  if (selectedItems.value.length === 0) return items.value[0]
  const randomId = selectedItems.value[Math.floor(Math.random() * selectedItems.value.length)]
  return items.value.find(i => i.id === randomId) || items.value[0]
})

const selectedFlameColors = computed(() =>
  flameColors.value.find(f => f.id === selectedFlame.value)?.colors || flameColors.value[0].colors
)

const generatePaper = (): BurningPaper => {
  const availableItems = selectedItems.value.length > 0 ? selectedItems.value : ['money']
  const randomId = availableItems[Math.floor(Math.random() * availableItems.length)]
  const item = items.value.find(i => i.id === randomId) || items.value[0]
  return {
    x: 20 + Math.random() * 60,
    y: -10,
    size: 35 + Math.random() * 15,
    rotation: Math.random() * 360,
    speed: 0.3 + Math.random() * 0.3,
    item,
    burned: false,
    burnProgress: 0,
    wobble: Math.random() * Math.PI * 2
  }
}

const spawnParticles = (x: number, y: number, count: number, type: 'flame' | 'spark' | 'smoke'): void => {
  const colors = selectedFlameColors.value
  for (let i = 0; i < count; i++) {
    if (type === 'flame') {
      particles.value.push({
        x: x + (Math.random() - 0.5) * 20,
        y,
        size: 8 + Math.random() * 15,
        life: 0,
        maxLife: 40 + Math.random() * 30,
        vx: (Math.random() - 0.5) * 3,
        vy: -2 - Math.random() * 3,
        color: colors[Math.floor(Math.random() * colors.length)],
        type: 'flame'
      })
    } else if (type === 'spark') {
      particles.value.push({
        x: x + (Math.random() - 0.5) * 10,
        y,
        size: 2 + Math.random() * 3,
        life: 0,
        maxLife: 60 + Math.random() * 40,
        vx: (Math.random() - 0.5) * 4,
        vy: -3 - Math.random() * 3,
        color: '#ffd700',
        type: 'spark'
      })
    } else {
      particles.value.push({
        x: x + (Math.random() - 0.5) * 8,
        y,
        size: 10 + Math.random() * 15,
        life: 0,
        maxLife: 80 + Math.random() * 40,
        vx: (Math.random() - 0.5) * 1,
        vy: -1 - Math.random() * 1.5,
        color: '#666',
        type: 'smoke'
      })
    }
  }
}

const spawnPaper = (): void => {
  if (!hasUserSelected.value) return
  if (papers.value.filter(p => !p.burned).length < 12) {
    papers.value.push(generatePaper())
  }
}

const draw = (): void => {
  if (!ctx || !canvasRef.value) return

  const canvas = canvasRef.value
  const width = canvas.width
  const height = canvas.height

  // Clear canvas
  ctx.clearRect(0, 0, width, height)

  // Draw papers
  papers.value.forEach(paper => {
    ctx!.save()
    ctx!.translate(paper.x * width / 100, paper.y * height / 100)
    ctx!.rotate(paper.rotation * Math.PI / 180)

    if (paper.burned) {
      // Draw burning effect - drift toward campfire then fade out
      const progress = paper.burnProgress / 100
      const targetX = 50 // campfire center
      const targetY = 90

      // Drift toward campfire
      const driftX = paper.x + (targetX - paper.x) * progress * 0.02
      const driftY = paper.y + (targetY - paper.y) * progress * 0.02
      paper.x = driftX
      paper.y = driftY
      paper.rotation += 2

      ctx!.globalAlpha = (1 - progress) * 0.6
      ctx!.fillStyle = '#f5e6d3'
      ctx!.fillRect(-paper.size/2, -paper.size/3, paper.size, paper.size * 0.65)
    } else {
      // Draw paper money
      ctx!.globalAlpha = 1
      const gradient = ctx!.createLinearGradient(-paper.size/2, -paper.size/4, paper.size/2, paper.size/4)
      gradient.addColorStop(0, '#f5e6d3')
      gradient.addColorStop(0.5, '#d4a574')
      gradient.addColorStop(1, '#c9956c')
      ctx!.fillStyle = gradient
      ctx!.fillRect(-paper.size/2, -paper.size/3, paper.size, paper.size * 0.65)

      // Draw emoji
      ctx!.font = `${paper.size * 0.5}px serif`
      ctx!.textAlign = 'center'
      ctx!.textBaseline = 'middle'
      ctx!.fillText(paper.item.emoji, 0, 0)

      // Border
      ctx!.strokeStyle = 'rgba(139, 69, 19, 0.3)'
      ctx!.lineWidth = 1
      ctx!.strokeRect(-paper.size/2 + 2, -paper.size/3 + 2, paper.size - 4, paper.size * 0.65 - 4)
    }

    ctx!.restore()
  })

  // Draw particles
  particles.value.forEach(p => {
    const alpha = 1 - p.life / p.maxLife

    if (p.type === 'smoke') {
      ctx!.globalAlpha = alpha * 0.3
      ctx!.fillStyle = p.color
      ctx!.beginPath()
      ctx!.arc(p.x * width / 100, p.y * height / 100, p.size, 0, Math.PI * 2)
      ctx!.fill()
    } else if (p.type === 'spark') {
      ctx!.globalAlpha = alpha
      const gradient = ctx!.createRadialGradient(
        p.x * width / 100, p.y * height / 100, 0,
        p.x * width / 100, p.y * height / 100, p.size
      )
      gradient.addColorStop(0, p.color)
      gradient.addColorStop(0.5, '#ff4500')
      gradient.addColorStop(1, 'transparent')
      ctx!.fillStyle = gradient
      ctx!.beginPath()
      ctx!.arc(p.x * width / 100, p.y * height / 100, p.size, 0, Math.PI * 2)
      ctx!.fill()
    } else {
      ctx!.globalAlpha = alpha
      const gradient = ctx!.createRadialGradient(
        p.x * width / 100, p.y * height / 100, 0,
        p.x * width / 100, p.y * height / 100, p.size
      )
      gradient.addColorStop(0, p.color)
      gradient.addColorStop(0.5, selectedFlameColors.value[1])
      gradient.addColorStop(1, 'transparent')
      ctx!.fillStyle = gradient
      ctx!.beginPath()
      ctx!.arc(p.x * width / 100, p.y * height / 100, p.size, 0, Math.PI * 2)
      ctx!.fill()
    }
  })

  // Draw campfire glow at bottom
  const glowGradient = ctx!.createRadialGradient(
    width / 2, height * 0.92, 0,
    width / 2, height * 0.92, height * 0.3
  )
  glowGradient.addColorStop(0, `${selectedFlameColors.value[0]}40`)
  glowGradient.addColorStop(0.5, `${selectedFlameColors.value[1]}20`)
  glowGradient.addColorStop(1, 'transparent')
  ctx!.globalAlpha = 0.6
  ctx!.fillStyle = glowGradient
  ctx!.beginPath()
  ctx!.arc(width / 2, height * 0.92, height * 0.3, 0, Math.PI * 2)
  ctx!.fill()
  ctx!.globalAlpha = 1
}

const update = (): void => {
  // Update papers
  papers.value.forEach((paper, index) => {
    if (paper.burned) {
      paper.burnProgress += 2
      if (paper.burnProgress >= 100) {
        papers.value.splice(index, 1)
      }
    } else {
      paper.y += paper.speed
      paper.wobble += 0.05
      paper.x += Math.sin(paper.wobble) * 0.4
      paper.rotation += 1.5

      if (paper.y > 70) {
        paper.burned = true
      }
    }
  })

  // Update particles
  particles.value.forEach((p, index) => {
    p.life++
    p.x += p.vx
    p.y += p.vy

    if (p.type === 'spark') {
      p.vy += 0.05
      p.size *= 0.98
    } else if (p.type === 'smoke') {
      p.size *= 1.02
      p.vx *= 0.99
    } else {
      p.size *= 0.96
    }

    if (p.life >= p.maxLife || p.size < 0.5) {
      particles.value.splice(index, 1)
    }
  })

  // Spawn campfire particles
  if (particles.value.length < 80) {
    const baseX = 50
    spawnParticles(baseX, 88, 2, 'flame')
    if (Math.random() > 0.7) {
      spawnParticles(baseX, 85, 1, 'spark')
    }
    if (Math.random() > 0.5) {
      spawnParticles(baseX, 80, 1, 'smoke')
    }
  }

  draw()
  animationId = requestAnimationFrame(update)
}

const onPaperClick = (event: MouseEvent): void => {
  if (!canvasRef.value || !hasUserSelected.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = ((event.clientX - rect.left) / rect.width) * 100
  const y = ((event.clientY - rect.top) / rect.height) * 100

  // Spawn papers near click position
  for (let i = 0; i < 3; i++) {
    const paper = generatePaper()
    paper.x = x + (Math.random() - 0.5) * 15
    paper.y = y + (Math.random() - 0.5) * 10
    paper.speed = 0.5 + Math.random() * 0.5
    papers.value.push(paper)
  }
}

const resizeCanvas = (): void => {
  if (!canvasRef.value) return
  const parent = canvasRef.value.parentElement
  if (parent) {
    canvasRef.value.width = parent.clientWidth
    canvasRef.value.height = parent.clientHeight
  }
}

onMounted(() => {
  if (!canvasRef.value) return

  ctx = canvasRef.value.getContext('2d')
  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)

  // Spawn initial papers
  for (let i = 0; i < 6; i++) {
    setTimeout(() => {
      if (hasUserSelected.value) {
        papers.value.push({
          ...generatePaper(),
          y: -5 + Math.random() * 20
        })
      }
    }, i * 150)
  }

  // Spawn papers periodically
  setInterval(spawnPaper, 600)

  // Start animation
  animationId = requestAnimationFrame(update)
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<template>
  <div class="relative w-full h-full bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 overflow-hidden">
    <!-- 控制面板 -->
    <div class="absolute top-4 left-4 right-4 z-10 flex flex-wrap gap-4 p-4 bg-slate-900/60 dark:bg-slate-900/80 backdrop-blur-md rounded-xl border border-slate-700/50 dark:border-slate-600/50">
      <!-- 祭拜对象 -->
      <div class="flex flex-col gap-1">
        <label class="text-[10px] uppercase tracking-wider text-amber-400/80 dark:text-amber-300">祭拜对象</label>
        <select
          v-model="selectedRecipient"
          class="bg-slate-800/80 dark:bg-slate-800 text-amber-100 dark:text-amber-200 border border-slate-600/50 dark:border-slate-500/50 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500/50 dark:focus:ring-amber-400/50"
          @change="hasUserSelected = true"
        >
          <option v-for="r in recipients" :key="r.id" :value="r.id">
            {{ r.name }} ({{ r.relation }})
          </option>
        </select>
      </div>

      <!-- 祭品 -->
      <div class="flex flex-col gap-1">
        <label class="text-[10px] uppercase tracking-wider text-amber-400/80 dark:text-amber-300">祭品</label>
        <div class="flex gap-1">
          <button
            v-for="item in items"
            :key="item.id"
            class="w-9 h-9 rounded-lg flex items-center justify-center text-lg transition-all duration-200"
            :class="selectedItems.includes(item.id)
              ? 'bg-amber-600/60 dark:bg-amber-700/60 ring-2 ring-amber-400 dark:ring-amber-300 scale-110'
              : 'bg-slate-700/50 dark:bg-slate-600/50 hover:bg-slate-600/50 dark:hover:bg-slate-500/50'"
            :title="`${item.name} - ${item.price}冥币`"
            @click="selectedItems.includes(item.id) ? selectedItems.splice(selectedItems.indexOf(item.id), 1) : selectedItems.push(item.id)"
          >
            {{ item.emoji }}
          </button>
        </div>
      </div>

      <!-- 火焰颜色 -->
      <div class="flex flex-col gap-1">
        <label class="text-[10px] uppercase tracking-wider text-amber-400/80 dark:text-amber-300">火焰</label>
        <div class="flex gap-1">
          <button
            v-for="flame in flameColors"
            :key="flame.id"
            class="w-9 h-9 rounded-lg flex items-center justify-center text-xs font-bold transition-all duration-200"
            :class="selectedFlame === flame.id ? 'ring-2 ring-white scale-110' : 'hover:scale-105'"
            :style="{ background: `linear-gradient(135deg, ${flame.colors[0]}, ${flame.colors[2]})` }"
            :title="flame.name"
            @click="selectedFlame = flame.id"
          />
        </div>
      </div>
    </div>

    <!-- 标题 -->
    <div class="absolute top-36 left-1/2 -translate-x-1/2 text-center z-10">
      <h2 class="text-3xl font-bold text-amber-100 dark:text-amber-200 mb-1">赛博烧纸</h2>
      <p class="text-sm text-amber-200/50 dark:text-amber-300/50">点击纸钱即可燃烧 · 送给 {{ selectedRecipientName }}</p>
    </div>

    <!-- Canvas -->
    <canvas
      ref="canvasRef"
      class="absolute inset-0 w-full h-full cursor-pointer"
      @click="onPaperClick"
    />

    <!-- 木柴 -->
    <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-64 h-16 pointer-events-none">
      <div class="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
        <div class="w-16 h-3 rounded-full rotate-12 shadow-md" style="background: linear-gradient(180deg, #8b4513 0%, #654321 100%);" />
        <div class="w-16 h-3 rounded-full -rotate-12 shadow-md" style="background: linear-gradient(180deg, #8b4513 0%, #654321 100%);" />
        <div class="w-16 h-3 rounded-full rotate-6 shadow-md" style="background: linear-gradient(180deg, #a0522d 0%, #8b4513 100%);" />
      </div>
    </div>
  </div>
</template>
