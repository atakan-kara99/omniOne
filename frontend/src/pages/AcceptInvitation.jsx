import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { acceptInvitation, validateInvitation } from '../api.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'

function AcceptInvitation() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [password, setPassword] = useState('')
  const [email, setEmail] = useState('')
  const [requiresPassword, setRequiresPassword] = useState(false)
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [validating, setValidating] = useState(true)

  useEffect(() => {
    let mounted = true
    async function validate() {
      setValidating(true)
      setError('')
      if (!token) {
        setError('Invitation token missing.')
        setValidating(false)
        return
      }
      try {
        const data = await validateInvitation(token)
        if (mounted) {
          setEmail(data?.email || '')
          setRequiresPassword(Boolean(data?.requiresPassword))
        }
      } catch (err) {
        if (mounted) {
          setError(err?.payload?.detail || err.message || 'Invalid invitation token.')
        }
      } finally {
        if (mounted) {
          setValidating(false)
        }
      }
    }

    validate()
    return () => {
      mounted = false
    }
  }, [token])

  async function handleSubmit(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    if (!token) {
      setError('Invitation token missing.')
      return
    }
    if (requiresPassword) {
      if (!password.trim()) {
        setError('Password is required.')
        return
      }
      if (!isValidPassword(password)) {
        setError(PASSWORD_REQUIREMENTS)
        return
      }
    }
    setLoading(true)
    try {
      await acceptInvitation(token, requiresPassword ? { password } : undefined)
      setStatus('Invitation accepted. You can now sign in.')
      setTimeout(() => navigate('/login'), 1000)
    } catch (err) {
      setError(err?.payload?.detail || err.message || 'Failed to accept invitation.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Accept Invitation</h1>
      <p className="muted">Complete the invitation to activate the account.</p>
      {validating ? <p className="muted">Validating invitation...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {status ? <p className="success">{status}</p> : null}
      {!validating && !error ? (
        <form className="form" onSubmit={handleSubmit}>
          {email ? (
            <label className="field">
              <input type="email" value={email} disabled />
            </label>
          ) : null}
          {requiresPassword ? (
            <label className="field">
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Password"
                autoComplete="new-password"
                minLength={8}
                maxLength={32}
                pattern={PASSWORD_PATTERN_STRING}
                title={PASSWORD_REQUIREMENTS}
                onInvalid={(event) => event.target.setCustomValidity(PASSWORD_REQUIREMENTS)}
                onInput={(event) => event.target.setCustomValidity('')}
                required
              />
            </label>
          ) : (
            <p className="muted">No password required. Confirm to accept the invitation.</p>
          )}
          <button type="submit" disabled={loading}>
            {loading ? 'Accepting...' : 'Accept invitation'}
          </button>
        </form>
      ) : null}
      <p className="hint">
        <Link to="/login">Back to sign in</Link>
      </p>
    </section>
  )
}

export default AcceptInvitation
