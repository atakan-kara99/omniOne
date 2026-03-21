export function buildWebSocketUrl() {
  const apiBase = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
  const wsBase = apiBase.replace(/^http/, 'ws')
  return `${wsBase}/ws`
}

export function formatChatTimestamp(value) {
  if (!value) return ''
  const date = new Date(value)
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const startOfDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const dayDiff = Math.round((startOfToday - startOfDate) / (1000 * 60 * 60 * 24))
  const time = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  if (dayDiff === 0) return `Today ${time}`
  if (dayDiff === 1) return `Yesterday ${time}`

  const startOfWeek = new Date(startOfToday)
  startOfWeek.setDate(startOfWeek.getDate() - ((startOfWeek.getDay() + 6) % 7))

  if (startOfDate >= startOfWeek) {
    const weekday = date.toLocaleDateString([], { weekday: 'long' })
    return `${weekday} ${time}`
  }

  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}.${month}.${year} ${time}`
}

export function formatMessageTime(value) {
  if (!value) return ''
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export function formatMessageDay(value) {
  if (!value) return ''
  const date = new Date(value)
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const startOfDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const dayDiff = Math.round((startOfToday - startOfDate) / (1000 * 60 * 60 * 24))

  if (dayDiff === 0) return 'Today'
  if (dayDiff === 1) return 'Yesterday'

  const startOfWeek = new Date(startOfToday)
  startOfWeek.setDate(startOfWeek.getDate() - ((startOfWeek.getDay() + 6) % 7))

  if (startOfDate >= startOfWeek) {
    return date.toLocaleDateString('en-US', { weekday: 'long' })
  }

  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}.${month}.${year}`
}
