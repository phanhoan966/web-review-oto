export interface PageMeta {
  page: number
  size: number
  total: number
}

export function resolvePageMeta(data: any, fallbackLength: number, current: PageMeta): PageMeta {
  const total = numberOrDefault(data?.total ?? data?.totalElements ?? data?.count, fallbackLength)
  const size = numberOrDefault(data?.size ?? data?.pageSize, current.size)
  const page = numberOrDefault(data?.page ?? data?.pageNumber ?? data?.number, current.page)
  return {
    page,
    size,
    total
  }
}

function numberOrDefault(value: any, fallback: number): number {
  const n = Number(value)
  if (Number.isFinite(n) && n >= 0) return n
  return fallback
}
