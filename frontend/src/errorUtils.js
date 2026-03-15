const AUTH_ERROR_CODES = new Set([
  'AUTH_INVALID_TOKEN',
  'AUTH_INVALID_CREDENTIALS',
  'AUTH_REFRESH_INVALID',
])

export function isAuthErrorCode(errorCode) {
  return AUTH_ERROR_CODES.has(errorCode)
}

function normalizeFieldErrors(errors) {
  if (!errors || typeof errors !== 'object' || Array.isArray(errors)) return null
  return errors
}

export function buildAppError({ status, detail, errorCode, traceId, fieldErrors, payload }) {
  const message = detail || 'Request failed.'
  const error = new Error(message)
  error.name = 'AppError'
  error.status = typeof status === 'number' ? status : 0
  error.detail = detail || message
  error.errorCode = errorCode || ''
  error.traceId = traceId || ''
  error.fieldErrors = normalizeFieldErrors(fieldErrors)
  error.payload = payload ?? null
  error.isAppError = true
  return error
}

export function toAppError(payload, status, statusText) {
  if (payload?.isAppError) return payload

  let detail = ''
  let errorCode = ''
  let traceId = ''
  let fieldErrors = null

  if (payload && typeof payload === 'object') {
    detail = payload.detail || payload.message || ''
    errorCode = payload.errorCode || ''
    traceId = payload.traceId || ''
    fieldErrors = payload.errors || payload.fieldErrors || null
  } else if (typeof payload === 'string') {
    detail = payload
  }

  if (!detail) {
    detail = statusText || 'Request failed.'
  }

  return buildAppError({
    status,
    detail,
    errorCode,
    traceId,
    fieldErrors,
    payload,
  })
}

export function formatErrorMessage(err) {
  if (!err) return ''

  const fallback = 'Something went wrong.'
  let detail = ''
  let traceId = ''

  if (typeof err === 'string') {
    const trimmed = err.trim()
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      try {
        const parsed = JSON.parse(trimmed)
        detail = parsed?.detail || parsed?.message || parsed?.title || ''
        traceId = parsed?.traceId || ''
      } catch {
        detail = ''
      }
    } else {
      detail = err
    }
  } else {
    detail = err.detail || err.message || ''
    traceId = err.traceId || ''
    if (!traceId && err.payload && typeof err.payload === 'string') {
      const trimmed = err.payload.trim()
      if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
        try {
          const parsed = JSON.parse(trimmed)
          traceId = parsed?.traceId || traceId
          detail = detail || parsed?.detail || parsed?.message || parsed?.title || ''
        } catch {
          // ignore
        }
      }
    }
  }

  const safeDetail = detail || fallback
  return safeDetail
}

export function getFieldErrors(err) {
  if (!err || typeof err === 'string') return null
  return err.fieldErrors || null
}
