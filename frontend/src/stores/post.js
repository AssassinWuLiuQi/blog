import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const usePostStore = defineStore('post', () => {
  // State
  const posts = ref([
    {
      id: 1,
      title: '深度学习在自然语言处理中的应用',
      summary: '本文探讨了深度学习技术在自然语言处理领域的重要进展，包括Transformer架构和注意力机制的核心原理。',
      content: '深度学习已彻底改变了自然语言处理的面貌。从Word2Vec到BERT，从RNN到Transformer，这些技术的演进让机器能够更好地理解和生成人类语言。\n\nTransformer架构通过自注意力机制解决了长距离依赖问题，使得模型能够并行处理序列数据，大大提高了训练效率。\n\n在具体应用方面，机器翻译、情感分析、问答系统等任务都取得了突破性进展。特别是GPT系列模型的出现，展示了大规模语言模型的强大能力。',
      category: 'AI',
      author: 'Scholar Admin',
      date: '2026-03-28',
      readTime: '8分钟',
      featured: true
    },
    {
      id: 2,
      title: 'Vue 3 Composition API 最佳实践',
      summary: '深入理解Vue 3的Composition API和响应式系统，包括setup语法糖、ref与reactive的使用场景。',
      content: 'Vue 3引入的Composition API是一种全新的逻辑组织方式，相比Options API，它提供了更灵活的代码组织能力。\n\n通过setup函数，我们可以在组件逻辑更加复杂时，将相关逻辑集中到一个函数中，而不是分散在不同的选项中。\n\nref和reactive是响应式系统的核心。ref用于基本类型数据，reactive用于对象类型。理解它们的区别和使用场景是掌握Vue 3的关键。',
      category: '前端',
      author: 'Scholar Admin',
      date: '2026-03-27',
      readTime: '12分钟',
      featured: false
    },
    {
      id: 3,
      title: '微服务架构设计原则',
      summary: '探讨微服务架构的核心设计原则与实践方法，包括服务拆分、API网关和服务治理。',
      content: '微服务架构已成为现代云原生应用的主流选择。它将大型应用拆分为多个小型、独立的服务，每个服务负责特定的业务功能。\n\n服务拆分需要遵循高内聚低耦合的原则，确保每个服务具有清晰的边界和单一职责。\n\nAPI网关作为系统的统一入口，负责请求路由、认证限流等功能。服务治理则包括服务发现、负载均衡、熔断器等模式。',
      category: '架构',
      author: 'Scholar Admin',
      date: '2026-03-26',
      readTime: '10分钟',
      featured: false
    },
    {
      id: 4,
      title: 'TypeScript 5.0 新特性解析',
      summary: '全面解析TypeScript 5.0带来的新特性和改进，包括类型参数装饰器和常量模板类型。',
      content: 'TypeScript 5.0引入了许多激动人心的新特性，进一步增强了类型系统的表达能力。\n\n常量模板类型使得在类型层面进行更复杂的字符串处理成为可能。类型参数装饰器则为类型操作提供了更灵活的机制。\n\n这些新特性使得TypeScript能够更好地支持大规模企业级应用的开发。',
      category: '前端',
      author: 'Scholar Admin',
      date: '2026-03-25',
      readTime: '6分钟',
      featured: false
    },
    {
      id: 5,
      title: '云原生时代的数据库选型',
      summary: '分析云原生环境下不同类型数据库的特点和适用场景，帮助开发者做出合理的技术选型。',
      content: '在云原生时代，数据库技术呈现出多样化的特点。从传统的关系型数据库到新兴的NewSQL，再到各种专用的NoSQL数据库。\n\nPostgreSQL作为功能最强大的开源关系型数据库，在云原生环境下焕发了新的活力。Redis作为内存数据库，在缓存和实时分析场景中不可或缺。\n\n选择合适的数据库需要综合考虑数据模型、一致性要求、性能需求和运维成本等因素。',
      category: '数据库',
      author: 'Scholar Admin',
      date: '2026-03-24',
      readTime: '9分钟',
      featured: false
    }
  ])

  const categories = ref([
    { id: 'all', name: '全部', subItems: [] },
    { id: 'frontend', name: '前端', subItems: ['Vue', 'React', 'TypeScript'] },
    { id: 'backend', name: '后端', subItems: ['Node.js', 'Python', 'Go'] },
    { id: 'architecture', name: '架构', subItems: ['微服务', '云原生', '设计模式'] },
    { id: 'ai', name: 'AI', subItems: ['深度学习', 'NLP', '计算机视觉'] },
    { id: 'database', name: '数据库', subItems: ['关系型', 'NoSQL', 'NewSQL'] }
  ])

  // Getters
  const recentPosts = computed(() => {
    return posts.value.slice(0, 5)
  })

  const featuredPosts = computed(() => {
    return posts.value.filter(post => post.featured)
  })

  // Actions
  function getPostById(id) {
    return posts.value.find(post => post.id === Number(id))
  }

  function getPostsByCategory(category) {
    if (!category || category === 'all') return posts.value
    return posts.value.filter(post => post.category.toLowerCase() === category.toLowerCase())
  }

  function searchPosts(query) {
    if (!query) return posts.value
    const lowerQuery = query.toLowerCase()
    return posts.value.filter(post =>
      post.title.toLowerCase().includes(lowerQuery) ||
      post.summary.toLowerCase().includes(lowerQuery)
    )
  }

  return {
    posts,
    categories,
    recentPosts,
    featuredPosts,
    getPostById,
    getPostsByCategory,
    searchPosts
  }
})