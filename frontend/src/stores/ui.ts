import { defineStore } from 'pinia'

type ThemeMode = 'light' | 'dark'

interface FilterChip {
  label: string
  value: string
  active: boolean
}

interface UiState {
  filters: FilterChip[]
  theme: ThemeMode
}

const storageKey = 'ui-theme'

export const useUiStore = defineStore('ui', {
  state: (): UiState => ({
    filters: [
      { label: 'Tìm, kiếm, mê', value: 'search', active: true },
      { label: 'Lỗi dùng', value: 'issue', active: false },
      { label: '50 triệu/mua', value: 'budget', active: false },
      { label: '5l/100km', value: 'economy', active: false }
    ],
    theme: 'light'
  }),
  actions: {
    toggleFilter(value: string) {
      this.filters = this.filters.map((chip) => ({ ...chip, active: chip.value === value }))
    },
    initTheme() {
      const stored = localStorage.getItem(storageKey) as ThemeMode | null
      const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
      const next = stored || (prefersDark ? 'dark' : 'light')
      this.applyTheme(next)
    },
    toggleTheme() {
      this.applyTheme(this.theme === 'dark' ? 'light' : 'dark')
    },
    applyTheme(theme: ThemeMode) {
      this.theme = theme
      localStorage.setItem(storageKey, theme)
      document.documentElement.setAttribute('data-theme', theme)
    }
  }
})
