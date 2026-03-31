export interface ArticleRequest {
  title: string
  content: string
  excerpt: string
  status: 'DRAFT' | 'PUBLISHED'
  categoryIds?: number[]
}

export interface ArticleResponse {
  id: number
  title: string
  excerpt: string
  status: string
  author: string
  createdAt: string
  publishedAt: string | null
  categories: string[]
  readTime: string
}

export interface ArticleDetailResponse extends ArticleResponse {
  content: string
  authorId: number
  updatedAt: string
}

export interface Post {
  id: number
  title: string
  summary: string
  content: string
  category: string
  author: string
  date: string
  readTime: string
  featured: boolean
}

export interface Category {
  id: string
  name: string
  subItems: string[]
}
