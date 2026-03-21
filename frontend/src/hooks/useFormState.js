import { useState } from 'react'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'

/**
 * useFormState — eliminates boilerplate for form status/error/loading state.
 *
 * Usage:
 *   const form = useFormState()
 *
 *   async function handleSubmit(event) {
 *     event.preventDefault()
 *     form.startSaving()
 *     try {
 *       await apiCall(payload)
 *       form.setSuccess('Saved.')
 *     } catch (err) {
 *       form.setFailure(err)
 *     }
 *   }
 *
 *   return (
 *     <>
 *       <StatusMessage status={form.status} error={form.error} />
 *       <FormField label="Name" error={form.fieldErrors?.name}>
 *         <input ... />
 *       </FormField>
 *       <Button loading={form.saving}>Save</Button>
 *     </>
 *   )
 */
export function useFormState() {
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState(null)
  const [saving, setSaving] = useState(false)

  function startSaving() {
    setStatus('')
    setError('')
    setFieldErrors(null)
    setSaving(true)
  }

  function setSuccess(message) {
    setStatus(message)
    setError('')
    setFieldErrors(null)
    setSaving(false)
  }

  function setFailure(err) {
    setStatus('')
    setError(formatErrorMessage(err))
    setFieldErrors(getFieldErrors(err))
    setSaving(false)
  }

  function setErrorManual(message) {
    setError(message)
    setSaving(false)
  }

  function reset() {
    setStatus('')
    setError('')
    setFieldErrors(null)
    setSaving(false)
  }

  return {
    status,
    error,
    fieldErrors,
    saving,
    startSaving,
    setSuccess,
    setFailure,
    setErrorManual,
    reset,
    setStatus,
    setError,
    setFieldErrors,
  }
}
