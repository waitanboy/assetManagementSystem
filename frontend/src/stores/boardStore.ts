import { defineStore } from 'pinia'
import api from '../api'

export interface BoardPost {
  id?: number
  title: string
  content: string
  authorId?: number
  authorName?: string
  authorEmail?: string
  viewCount?: number
  createdAt?: string
  updatedAt?: string
}

export interface BoardComment {
  id?: number
  postId: number
  authorId?: number
  authorName?: string
  authorEmail?: string
  content: string
  createdAt?: string
  updatedAt?: string
}

export const useBoardStore = defineStore('board', {
  state: () => ({
    posts: [] as BoardPost[],
    currentPost: null as BoardPost | null,
    comments: [] as BoardComment[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchPosts(search?: string) {
      this.loading = true
      try {
        const response = await api.get<BoardPost[]>('/board/posts', {
          params: { search }
        })
        this.posts = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch posts'
      } finally {
        this.loading = false
      }
    },

    async fetchPost(id: number) {
      this.loading = true
      try {
        const response = await api.get<BoardPost>(`/board/posts/${id}`)
        this.currentPost = response.data
        await this.fetchComments(id)
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch post'
      } finally {
        this.loading = false
      }
    },

    async createPost(post: BoardPost) {
      try {
        await api.post('/board/posts', post)
        await this.fetchPosts()
      } catch (err: any) {
        throw err
      }
    },

    async updatePost(id: number, post: BoardPost) {
      try {
        await api.put(`/board/posts/${id}`, post)
        await this.fetchPost(id)
      } catch (err: any) {
        throw err
      }
    },

    async deletePost(id: number) {
      try {
        await api.delete(`/board/posts/${id}`)
        this.posts = this.posts.filter(p => p.id !== id)
      } catch (err: any) {
        throw err
      }
    },

    async fetchComments(postId: number) {
      try {
        const response = await api.get<BoardComment[]>(`/board/posts/${postId}/comments`)
        this.comments = response.data
      } catch (err: any) {
        console.error('Failed to fetch comments', err)
      }
    },

    async addComment(postId: number, content: string) {
      try {
        await api.post(`/board/posts/${postId}/comments`, { content })
        await this.fetchComments(postId)
      } catch (err: any) {
        throw err
      }
    },

    async deleteComment(id: number, postId: number) {
      try {
        await api.delete(`/board/comments/${id}`)
        await this.fetchComments(postId)
      } catch (err: any) {
        throw err
      }
    },
  },
})
