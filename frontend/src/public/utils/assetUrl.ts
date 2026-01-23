const rawBase = (import.meta.env.VITE_FILE_BASE_URL || import.meta.env.VITE_API_URL || '').replace(/\/$/, '')
const assetBase = rawBase.replace(/\/api$/, '')

export function buildAssetUrl(path?: string) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path) || path.startsWith('//')) return path
  const normalized = path.startsWith('/') ? path : `/${path}`
  return `${assetBase}${normalized}`
}
