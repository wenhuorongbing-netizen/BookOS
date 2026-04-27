import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { BookRecord } from '../types'

export type CaptureMarker = 'text' | 'quote' | 'link' | 'emoji' | 'inspiration' | 'favorite' | 'important' | 'question' | 'idea'

export interface RecentNoteBlock {
  id: string
  bookId: number
  bookTitle: string
  type: CaptureMarker
  title: string
  content: string
  tags: string[]
  page: string | null
  createdAt: string
  bookmarked: boolean
}

export const useCaptureStore = defineStore('capture', () => {
  const recentBlocks = ref<RecentNoteBlock[]>([])

  const latestBlocks = computed(() =>
    [...recentBlocks.value].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
  )

  function addCapture(book: BookRecord, content: string, marker: CaptureMarker) {
    const parsed = parseCapture(content)
    const block: RecentNoteBlock = {
      id: `${book.id}-${Date.now()}`,
      bookId: book.id,
      bookTitle: book.title,
      type: marker,
      title: parsed.title,
      content: parsed.content,
      tags: parsed.tags,
      page: parsed.page,
      createdAt: new Date().toISOString(),
      bookmarked: false,
    }

    recentBlocks.value.unshift(block)
    return block
  }

  function toggleBookmark(id: string) {
    const block = recentBlocks.value.find((item) => item.id === id)
    if (block) block.bookmarked = !block.bookmarked
  }

  function forBook(bookId: number) {
    return latestBlocks.value.filter((block) => block.bookId === bookId)
  }

  return {
    recentBlocks,
    latestBlocks,
    addCapture,
    toggleBookmark,
    forBook,
  }
})

function parseCapture(raw: string) {
  const content = raw.trim()
  const firstLine = content.split(/\r?\n/).find(Boolean) ?? 'Untitled capture'
  const pageMatch = content.match(/\b(?:p\.?|page|pp\.?)\s*(\d+(?:\s*[-\u2013]\s*\d+)?)\b/i)
  const hashTags = [...content.matchAll(/#([\p{L}\p{N}_-]+)/gu)].map((match) => match[1])
  const wikiTags = [...content.matchAll(/\[\[([^\]]+)\]\]/g)].map((match) => match[1].trim()).filter(Boolean)

  return {
    title: firstLine.length > 72 ? `${firstLine.slice(0, 69)}...` : firstLine,
    content,
    page: pageMatch?.[1]?.replace(/\s+/g, '') ?? null,
    tags: [...new Set([...hashTags, ...wikiTags])].slice(0, 6),
  }
}
