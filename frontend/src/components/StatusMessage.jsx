import { formatErrorMessage } from '../errorUtils.js'

/**
 * StatusMessage — renders success and/or error messages.
 *
 * Usage:
 *   <StatusMessage status={form.status} error={form.error} />
 */
export default function StatusMessage({ status, error }) {
  if (!status && !error) return null
  return (
    <>
      {status ? <p className="success">{status}</p> : null}
      {error ? <p className="error">{typeof error === 'string' ? error : formatErrorMessage(error)}</p> : null}
    </>
  )
}
