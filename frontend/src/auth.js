const USER_KEY = 'omniOne.user'
let memoryToken = ''

export function getToken() {
  return memoryToken || ''
}

export function setToken(token) {
  memoryToken = token || ''
}

export function clearToken() {
  memoryToken = ''
}



export function getStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function setStoredUser(user) {
  if (!user) {
    localStorage.removeItem(USER_KEY)
    return
  }
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearStoredUser() {
  localStorage.removeItem(USER_KEY)
}
