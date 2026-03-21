import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { acceptInvitation, validateInvitation } from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function AcceptInvitation() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [password, setPassword] = useState('')
  const [email, setEmail] = useState('')
  const [requiresPassword, setRequiresPassword] = useState(false)
  const form = useFormState()

  const { loading: validating, error: validationError } = useLoadData(async () => {
    if (!token) {
      throw new Error('Invitation token missing.')
    }
    const data = await validateInvitation(token)
    setEmail(data?.email || '')
    setRequiresPassword(Boolean(data?.requiresPassword))
  }, [token])

  async function handleSubmit(event) {
    event.preventDefault()
    form.reset()

    if (!token) {
      form.setErrorManual('Invitation token missing.')
      return
    }

    if (requiresPassword) {
      if (!password.trim()) {
        form.setErrorManual('Password is required.')
        return
      }
      if (!isValidPassword(password)) {
        form.setError(PASSWORD_REQUIREMENTS)
        return
      }
    }

    form.startSaving()
    try {
      await acceptInvitation(token, requiresPassword ? { password } : undefined)
      form.setSuccess('Invitation accepted. You can now sign in.')
      setTimeout(() => navigate('/login'), 1000)
    } catch (err) {
      form.setFieldErrors(getFieldErrors(err))
      form.setFailure(formatErrorMessage(err) || 'Failed to accept invitation.')
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Accept Invitation</h1>
      <p className="muted">Complete the invitation to activate the account.</p>
      {validating ? <p className="muted">Validating invitation...</p> : null}
      <StatusMessage status={form.status} error={form.error || validationError} />
      {!validating && !validationError ? (
        <form className="form" onSubmit={handleSubmit} autoComplete="off">
          {email ? (
            <FormField label="Email">
              <input
                type="email"
                name="email"
                id="invite-email"
                autoComplete="off"
                value={email}
                disabled
              />
            </FormField>
          ) : null}
          {requiresPassword ? (
            <FormField label="Password" error={form.fieldErrors?.password}>
              <input
                type="password"
                name="password"
                id="invite-password"
                autoComplete="off"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Password"
                minLength={8}
                maxLength={32}
                pattern={PASSWORD_PATTERN_STRING}
                title={PASSWORD_REQUIREMENTS}
                onInvalid={(event) => event.target.setCustomValidity(PASSWORD_REQUIREMENTS)}
                onInput={(event) => event.target.setCustomValidity('')}
                required
              />
            </FormField>
          ) : (
            <p className="muted">No password required. Confirm to accept the invitation.</p>
          )}
          <Button type="submit" loading={form.saving} loadingText="Accepting...">
            Accept invitation
          </Button>
        </form>
      ) : null}
      <p className="hint">
        <Link to="/login">Back to sign in</Link>
      </p>
    </section>
  )
}

export default AcceptInvitation
