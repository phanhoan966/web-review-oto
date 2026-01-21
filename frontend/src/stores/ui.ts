import { defineStore } from 'pinia'

interface FilterChip {
  label: string
  value: string
  active: boolean
}

interface UiState {
  filters: FilterChip[]
}

export const useUiStore = defineStore('ui', {
  state: (): UiState => ({
    filters: [
      { label: 'Tìm, kiếm, mê', value: 'search', active: true },
      { label: 'Lỗi dùng', value: 'issue', active: false },
      { label: '50 triệu/mua', value: 'budget', active: false },
      { label: '5l/100km', value: 'economy', active: false }
    ]
  }),
  actions: {
    toggleFilter(value: string) {
      this.filters = this.filters.map((chip) => ({ ...chip, active: chip.value === value }))
    }
  }
})
