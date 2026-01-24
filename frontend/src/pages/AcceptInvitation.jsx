import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { acceptInvitation } from '../api.js'

function AcceptInvitation() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [password, setPassword] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    if (!token) {
      setError('Invitation token missing.')
      return
    }
    setLoading(true)
    try {
      await acceptInvitation(token, { password })
      setStatus('Invitation accepted. You can now sign in.')
    } catch (err) {
      setError(err.message || 'Failed to accept invitation.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Accept Invitation</h1>
      <p className="muted">Set your password to activate the account.</p>
      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Password"
            autoComplete="new-password"
            required
          />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Accepting...' : 'Accept invitation'}
        </button>
        {status ? <p className="success">{status}</p> : null}
        {error ? <p className="error">{error}</p> : null}
      </form>
      <p className="hint">
        <Link to="/login">Back to sign in</Link>
      </p>
    </section>
  )
}

export default AcceptInvitation
