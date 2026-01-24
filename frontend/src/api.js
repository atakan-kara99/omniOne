import { clearStoredUser, clearToken, getToken } from './auth.js'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
const DEBUG_API = import.meta.env.DEV

function logApi(event, details) {
  if (!DEBUG_API) return
  try {
    console.info(`[api] ${event}`, details || '')
  } catch {
    // ignore logging failures
  }
}

function buildHeaders(customHeaders) {
  const headers = new Headers(customHeaders || {})
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  return headers
}

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  return response.text()
}

export async function apiFetch(path, options = {}) {
  let response
  logApi('request', { path, method: options.method || 'GET', body: options.body })
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: buildHeaders(options.headers),
    })
  } catch (err) {
    logApi('error', { path, message: err?.message || 'Network error' })
    const error = new Error('Service currently unavailable. Please try again later.')
    error.status = 0
    error.payload = null
    throw error
  }

  if (!response.ok) {
    const payload = await parseResponse(response)
    logApi('response-error', { path, status: response.status, payload })
    const error = new Error(payload?.message || response.statusText)
    error.status = response.status
    error.payload = payload
    if (response.status === 401) {
      clearToken()
      clearStoredUser()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    throw error
  }

  if (response.status === 204) {
    logApi('response', { path, status: response.status, payload: null })
    return null
  }

  const payload = await parseResponse(response)
  logApi('response', { path, status: response.status, payload })
  return payload
}

export function login(payload) {
  return apiFetch('/auth/account/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function register(payload) {
  return apiFetch('/auth/account/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function activateAccount(token) {
  return apiFetch(`/auth/account/activate?token=${encodeURIComponent(token)}`, {
    method: 'GET',
  })
}

export function forgotPassword(email) {
  return apiFetch(`/auth/password/forgot?email=${encodeURIComponent(email)}`, {
    method: 'GET',
  })
}

export function resetPassword(token, payload) {
  return apiFetch(`/auth/password/reset?token=${encodeURIComponent(token)}`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function acceptInvitation(token, payload) {
  return apiFetch(`/auth/invitation/accept?token=${encodeURIComponent(token)}`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getUser() {
  return apiFetch('/user', { method: 'GET' })
}

export function deleteUser() {
  return apiFetch('/user', { method: 'DELETE' })
}

export function getProfile() {
  return apiFetch('/user/profile', { method: 'GET' })
}

export function updateProfile(payload) {
  return apiFetch('/user/profile', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function changePassword(payload) {
  return apiFetch('/user/password', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getCoach() {
  return apiFetch('/coach', { method: 'GET' })
}

export function updateCoach(payload) {
  return apiFetch('/coach', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function getCoachClients() {
  return apiFetch('/coach/clients', { method: 'GET' })
}

export function getCoachClient(clientId) {
  return apiFetch(`/coach/clients/${clientId}`, { method: 'GET' })
}

export function endCoaching(clientId) {
  return apiFetch(`/coach/clients/${clientId}`, { method: 'DELETE' })
}

export function inviteClient(email) {
  return apiFetch(`/coach/clients/invite?email=${encodeURIComponent(email)}`, {
    method: 'GET',
  })
}

export function getCoachClientPlans(clientId) {
  return apiFetch(`/coach/clients/${clientId}/nutri-plans`, {
    method: 'GET',
  })
}

export function getCoachClientActivePlan(clientId) {
  return apiFetch(`/coach/clients/${clientId}/nutri-plans/active`, {
    method: 'GET',
  })
}

export function addCoachClientPlan(clientId, payload) {
  return apiFetch(`/coach/clients/${clientId}/nutri-plans`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateCoachClientPlan(clientId, planId, payload) {
  return apiFetch(
    `/coach/clients/${clientId}/nutri-plans/${planId}`,
    {
      method: 'PUT',
      body: JSON.stringify(payload),
    },
  )
}

export function getCoachQuestions() {
  return apiFetch('/coach/questionnaire', { method: 'GET' })
}

export function addCoachQuestion(payload) {
  return apiFetch('/coach/questionnaire', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteCoachQuestion(questionId) {
  return apiFetch(`/coach/questionnaire/${questionId}`, { method: 'DELETE' })
}

export function getCoachClientAnswers(clientId) {
  return apiFetch(`/coach/questionnaire/${clientId}`, { method: 'GET' })
}

export function getClient() {
  return apiFetch('/client', { method: 'GET' })
}

export function updateClient(payload) {
  return apiFetch('/client', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function getClientActivePlan() {
  return apiFetch('/client/nutri-plans/active', { method: 'GET' })
}

export function getClientPlans() {
  return apiFetch('/client/nutri-plans', { method: 'GET' })
}

export function getClientQuestionnaire() {
  return apiFetch('/client/questionnaire', { method: 'GET' })
}

export function getClientAnswers() {
  return apiFetch('/client/questionnaire/answers', { method: 'GET' })
}

export function updateClientAnswers(payload) {
  return apiFetch('/client/questionnaire/answers', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function endClientCoaching() {
  return apiFetch('/client/coach', { method: 'DELETE' })
}

export function getClientCoach() {
  return apiFetch('/client/coach', { method: 'GET' })
}

export function getChats() {
  return apiFetch('/user/chats', { method: 'GET' })
}

export function getChat(conversationId) {
  return apiFetch(`/user/chats/${conversationId}`, { method: 'GET' })
}

export function getChatMessages(conversationId, options = {}) {
  const params = new URLSearchParams()
  if (options.size) params.set('size', String(options.size))
  if (options.beforeSentAt) params.set('beforeSentAt', options.beforeSentAt)
  const query = params.toString()
  return apiFetch(
    `/user/chats/${conversationId}/messages${query ? `?${query}` : ''}`,
    { method: 'GET' },
  )
}

export function startChat(userId) {
  return apiFetch(`/user/chats/start/${userId}`, { method: 'GET' })
}
